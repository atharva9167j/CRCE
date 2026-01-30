# Implementation Guide & Visual Architecture Reference

## 1. Attack Timeline Diagram (ASCII Flow)

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SYSTEM BOOT SEQUENCE                             │
└─────────────────────────────────────────────────────────────────────┘

TIME    PHASE                    COMPONENT                ACTION
────    ─────────────────────────────────────────────────────────────

T0      [FIRMWARE]              BIOS/UEFI               Normal startup
        ↓
T1      [EXTERNAL MEDIA]        Malicious Bootloader    USB/ODD boot
        ↓
T2      [FIRMWARE]              ExitBootServices        Register hook
        ↓
T3      [PAYLOAD]               Malicious Driver        Allocate memory
        ↓
T4      [KERNEL]                ntoskrnl.exe            Load (Kernel)
        ↓
T5      [DRIVER LOAD]           ci.dll bypass           Disable DSE
        ↓
T6      [KERNEL INIT]           Ring 0 execution        Patch loaded
        ↓
T7      [PROCESS LOAD]          LSASS startup           Target located
        ↓
T8      [PATCHING]              Memory modification     msv1_0.dll hooked
        ↓
T9      [READY]                 System ready            Backdoor active
        ↓
T10     [USER INPUT]            Login screen            User enters password
        ↓
T11     [AUTHENTICATION]        msv1_0!Validate         Patched func returns 0
        ↓
T12     [SUCCESS]               Session created         Attacker has access

TIME ELAPSED: ~30-50 seconds from boot to login success
FORENSIC ARTIFACTS: None on disk
PERSISTENCE: Volatile (lost on reboot)
```

## 2. Memory Layout During Attack

```
FIRMWARE STAGE - UEFI BOOT
════════════════════════════════════════════════════════════════════

Physical Memory:
┌──────────────────────────────────────────────────────────────────┐
│ 0x00000000 - 0x000FFFFF   │ Real Mode IVT & BIOS Data             │
├──────────────────────────────────────────────────────────────────┤
│ 0x00100000 - 0x1FFFFFFF   │ Lower Memory (BIOS/UEFI runtime)      │
├──────────────────────────────────────────────────────────────────┤
│ 0x20000000 - 0x2FFFFFFF   │ EfiRuntimeServicesData                │
│                           │ ↳ MALICIOUS DRIVER ALLOCATED HERE      │
├──────────────────────────────────────────────────────────────────┤
│ 0x30000000 - end          │ System Memory                          │
└──────────────────────────────────────────────────────────────────┘

UEFI Services Active: Yes
Virtual Addressing: No (Physical addressing)
Protection Rings: Ring 0+ (Firmware level)


KERNEL STAGE - RING 0 EXECUTION
════════════════════════════════════════════════════════════════════

Virtual Address Space (Kernel):
┌──────────────────────────────────────────────────────────────────┐
│ 0xFFFF800000000000 - 0xFFFF807FFFFFFFFF   │ Kernel Image        │
│                                           │ (ntoskrnl.exe)      │
├──────────────────────────────────────────────────────────────────┤
│ 0xFFFF808000000000 - 0xFFFFFFFFFFFFFFFF   │ Kernel Heap/Pools   │
│                                           │ ↳ Malicious driver  │
│                                           │    now mapped here   │
├──────────────────────────────────────────────────────────────────┤
│ 0x00000000 - 0x7FFFFFFF                   │ User Space          │
│                                           │ (LSASS when         │
│                                           │  attached here)     │
└──────────────────────────────────────────────────────────────────┘

UEFI Services Active: No
Virtual Addressing: Yes (Paging enabled)
Protection Rings: Ring 0 (Kernel) / Ring 3 (User)


LSASS CONTEXT - ATTACHED VIA KeStackAttachProcess
════════════════════════════════════════════════════════════════════

Virtual Address Space (LSASS Process):
┌──────────────────────────────────────────────────────────────────┐
│ 0x7FFF0000 - 0x7FFFFFFF                   │ NTDLL (User Mode)   │
├──────────────────────────────────────────────────────────────────┤
│ 0x77F10000 - 0x77F50000                   │ msv1_0.dll          │
│                                           │ ↳ PATCH HERE!       │
│                                           │   MsvpPasswordVal   │
├──────────────────────────────────────────────────────────────────┤
│ 0x400000 - 0x77ED0000                     │ Other DLLs          │
├──────────────────────────────────────────────────────────────────┤
│ 0x00400000 - 0x00410000                   │ LSASS Code          │
└──────────────────────────────────────────────────────────────────┘

CR3 Register: Points to LSASS Page Tables
Current Process: lsass.exe (Ring 0 code, LSASS address space)
Access Type: Read/Write (Ring 0 privilege)
```

## 3. Attack Sequence Flowchart

```
START: System Powered On
│
├─→ UEFI/BIOS Initialization
│   │
│   ├─→ Check boot media order
│   │
│   └─→ [ATTACKER BOOTS FROM USB]
│       │
│       ├─→ Malicious BOOTX64.EFI executes
│       │
│       └─→ Hook ExitBootServices callback
│           │
│           └─→ Allocate EfiRuntimeServicesData
│               │
│               └─→ memcpy(malicious_driver)
│
├─→ Windows Boot Manager (bootmgr.efi)
│   │
│   └─→ ExitBootServices() called
│       │
│       ├─→ [CALLBACK FIRES]
│       │
│       └─→ Payload driver still in memory
│
├─→ Kernel Load (ntoskrnl.exe)
│   │
│   └─→ Driver Signature Enforcement (DSE)
│       │
│       ├─→ [PAYLOAD PATCHES g_CiOptions]
│       │
│       └─→ DSE disabled silently
│
├─→ Kernel Initialization (KiSystemStartup)
│   │
│   └─→ PatchGuard initialization
│       │
│       ├─→ [PAYLOAD EXECUTES EARLY]
│       │
│       └─→ No PatchGuard checks yet
│
├─→ System Services Starting
│   │
│   └─→ LSASS process (lsass.exe) spawns
│       │
│       ├─→ [DRIVER LOCATES LSASS]
│       │
│       ├─→ Find EPROCESS via PsInitialSystemProcess
│       │
│       ├─→ Traverse ActiveProcessLinks
│       │
│       └─→ Locate ImageFileName == "lsass.exe"
│
├─→ Module Loading
│   │
│   └─→ LSASS loads msv1_0.dll
│       │
│       ├─→ [DRIVER ATTACHES TO LSASS]
│       │
│       ├─→ KeStackAttachProcess(lsass_eprocess)
│       │
│       ├─→ Traverse PEB.Ldr.InLoadOrderModuleList
│       │
│       └─→ Locate msv1_0.dll base address
│
├─→ Function Hooking
│   │
│   └─→ Signature scan for MsvpPasswordValidate
│       │
│       ├─→ [FIND COMPARISON INSTRUCTION]
│       │
│       ├─→ Replace with XOR EAX, EAX; RET
│       │
│       └─→ FlushInstructionCache()
│
├─→ System Ready
│   │
│   └─→ User Login Screen
│       │
│       └─→ [ATTACK COMPLETE]
│
├─→ User Enters Credentials
│   │
│   └─→ Any password accepted
│       │
│       └─→ [LSASS CALLS PATCHED FUNCTION]
│           │
│           ├─→ MsvpPasswordValidate()
│           │
│           ├─→ XOR EAX, EAX (EAX = 0)
│           │
│           ├─→ RET (return STATUS_SUCCESS)
│           │
│           └─→ Windows grants access!
│
└─→ END: Attacker has system access (volatile)


SECURITY CHECKS BYPASSED:
═════════════════════════
✓ Driver Signature Enforcement (DSE)
✓ Kernel Patch Protection (PatchGuard) 
✓ NTLM Password Validation
✓ User Authorization
✗ BitLocker (if enabled - FAILS)
✗ Credential Guard (if enabled - FAILS)
✗ Virtualization-Based Security (if enabled - FAILS)
```

## 4. Ring Privilege Level Diagram

```
PRIVILEGE LEVELS IN x86/x64 ARCHITECTURE

Ring 0 (Kernel Mode)
┌─────────────────────────────────────────────────────────────┐
│ ✓ Full hardware access                                      │
│ ✓ Direct I/O                                                │
│ ✓ All CPU instructions allowed                              │
│ ✓ CR3 modification (context switch)                         │
│ ✓ Interrupt controller access                               │
│                                                              │
│ COMPONENTS:                                                  │
│ • ntoskrnl.exe (Windows Kernel)                             │
│ • drivers (*.sys)                                            │
│ • [ATTACKER PAYLOAD] ← Our malicious driver                │
└─────────────────────────────────────────────────────────────┘
           ▲
           │ Privilege level crossing (System call)
           │ Only through syscall interface
           ▼
Ring 3 (User Mode)
┌─────────────────────────────────────────────────────────────┐
│ ✗ Limited to allocated memory                               │
│ ✗ No direct I/O (except specific ports)                     │
│ ✗ Restricted CPU instructions                               │
│ ✗ Cannot modify CR3                                          │
│                                                              │
│ COMPONENTS:                                                  │
│ • User applications (calc.exe, notepad.exe, etc.)           │
│ • lsass.exe ← Our patching target                           │
│ • Services (explorer.exe, services.exe, etc.)               │
└─────────────────────────────────────────────────────────────┘


THE VULNERABILITY CHAIN:

User Mode → Kernel Mode  (can only go via syscall)
Firmware → Kernel Mode   (automatic, no check)
Bootloader → Firmware    (automatic, no check)
Attacker → Bootloader    (if physical access)

This creates a privilege escalation path:
Attacker (physical) → Bootloader → Firmware → Ring 0 → User Mode Access
```

## 5. EPROCESS Traversal Diagram

```
KERNEL PROCESS MANAGEMENT - EPROCESS CIRCULAR LIST

System Process (PID 4)
┌─────────────────────┐
│ EPROCESS            │
│ ├─ PID: 4           │
│ ├─ ImageFileName: "System"
│ ├─ ActiveProcessLinks.Flink
│ │  └──────┐
│ └─────────┼────────────┐
│           │ (Previous) │
│           │            │
│           ▼            │
    Kernel Processes     │
    ┌─────────────────────┤
    │ EPROCESS            │
    │ ├─ PID: 96          │
    │ ├─ ImageFileName: "smss.exe"
    │ ├─ ActiveProcessLinks.Flink
    │ │  └──────┐
    │ └─────────┼────────────┐
    │           │            │
    │           ▼            │
              Services        │
    ┌─────────────────────┤
    │ EPROCESS            │
    │ ├─ PID: 248         │
    │ ├─ ImageFileName: "csrss.exe"
    │ ├─ ActiveProcessLinks.Flink
    │ │  └──────┐
    │ └─────────┼────────────┐
    │           │            │
    │           ▼            │
             LSASS           │
    ┌─────────────────────┤
    │ EPROCESS            │
    │ ├─ PID: 256         │ ◄─── OUR TARGET
    │ ├─ ImageFileName: "lsass.exe" ◄─── SIGNATURE MATCH
    │ ├─ ActiveProcessLinks.Flink
    │ │  └──────┐
    │ └─────────┼────────────┐
    │           │            │
    │           ▼            │
           svchost.exe        │
    ┌─────────────────────┤
    │ EPROCESS            │
    │ ├─ PID: 384         │
    │ ├─ ImageFileName: "svchost.exe"
    │ ├─ ActiveProcessLinks.Flink
    │ │  └──────┐
    │ └─────────┼────────────┘
    │           │
    │           ▼
    │      (back to System)
    └───────────────────


KERNEL DRIVER TRAVERSAL ALGORITHM:

CurrentProc = PsInitialSystemProcess (System, PID 4)
ListHead = &CurrentProc.ActiveProcessLinks

do {
    if (CurrentProc.ImageFileName == "lsass.exe") {
        FOUND! 
        break
    }
    
    CurrentProc = NextEntry in ActiveProcessLinks
    
} while (CurrentProc != ListHead)


OFFSET CALCULATION:
───────────────────
ActiveProcessLinks offset = 0x190 (varies by Windows version)
ImageFileName offset = 0x440 (varies by Windows version)

To get ImageFileName from EPROCESS:
    char* name = (char*)((uintptr_t)eprocess + 0x440)
    
To get next EPROCESS in list:
    PEPROCESS next = (PEPROCESS)((uintptr_t)eprocess + 0x190)
```

## 6. Memory Protection & CR3 Register

```
VIRTUAL ADDRESS TRANSLATION & CONTEXT SWITCHING

Process A - Firefox
┌─────────────────────────────────────────┐
│ Virtual Address Space:                  │
│ 0x400000: Firefox code                  │
│ 0x7FFF0000: ntdll.dll                   │
│                                          │
│ CR3 Register = 0x150000 (Page Table A)   │
└─────────────────────────────────────────┘
           │
           │ CPU translates via Page Tables
           │ 0x400000 → 0x1A3000 (Physical)
           │
           ▼
PHYSICAL MEMORY
┌──────────────┐
│ 0x1A3000:    │ Firefox code
│ 0x2B4000:    │ ntdll.dll
│ ...          │
└──────────────┘


Process B - LSASS
┌─────────────────────────────────────────┐
│ Virtual Address Space:                  │
│ 0x400000: LSASS code                    │
│ 0x77F10000: msv1_0.dll ◄── TARGET      │
│                                          │
│ CR3 Register = 0x250000 (Page Table B)   │
└─────────────────────────────────────────┘
           │
           │ CPU translates via Page Tables
           │ 0x77F10000 → 0x3C5000 (Physical)
           │
           ▼
PHYSICAL MEMORY
┌──────────────┐
│ 0x3C5000:    │ msv1_0.dll
│ 0x3D0000:    │ LSASS code
│ ...          │
└──────────────┘


CONTEXT SWITCHING WITH KeStackAttachProcess:

Before Attach:
───────────────
CR3 = PageTableKernel
Address 0x77F10000 → Not mapped (LSASS memory inaccessible)


KeStackAttachProcess(LSASS):
────────────────────────────
// Kernel driver calls this API
KeStackAttachProcess(&LsassEprocess, &ApcState)
{
    // Internally:
    CR3 = LSASS_PageTableB  // Switch to LSASS page tables!
}

Now:
────
CR3 = PageTableLSASS
Address 0x77F10000 → Maps to 0x3C5000 (LSASS memory)

Kernel driver can now:
  ✓ Read from 0x77F10000 (msv1_0.dll)
  ✓ Write to 0x77F10000 (patch location!)
  ✓ Execute LSASS functions
  ✓ Complete the attack


After Detach:
─────────────
KeUnstackDetachProcess(&ApcState)
{
    // Internally:
    CR3 = KernelPageTable  // Restore kernel context
}

CR3 = PageTableKernel
Address 0x77F10000 → Not mapped again
```

## 7. Patch Location Discovery

```
SIGNATURE SCANNING FOR MsvpPasswordValidate

Scanner looks for:
1. MD4 hash computation pattern
2. Followed by comparison logic
3. Followed by conditional jump on result

Pseudocode Pattern:
───────────────────
[MD4 Computation]
  48 8D 15 ?? ?? ?? ??    ; lea rdx, [rel string]
  48 C1 E9 03              ; shr rcx, 3
  F3 A4                    ; rep movsb

[Comparison Logic]
  48 39 D0                 ; cmp rax, rdx
  75 0A                    ; jne Success (fail if not equal)

[Success Path]
  31 C0                    ; xor eax, eax (STATUS_SUCCESS)
  C3                       ; ret


SCANNING PROCESS:
─────────────────

msv1_0.dll base: 0x77F10000
Scan size: 0x100000 bytes

for (addr = 0x77F10000; addr < 0x77F10000 + 0x100000; addr++) {
    if (matches_md4_pattern(addr) &&
        matches_comparison_pattern(addr + 20) &&
        matches_success_pattern(addr + 30)) {
        
        FunctionBase = addr - MDPatternOffset
        return FunctionBase
    }
}

Result: MsvpPasswordValidate found at 0x77F10F50


PATCH APPLICATION:
───────────────────

Location: 0x77F10F50 + Comparison_Instruction_Offset
        = 0x77F10F5E

Original (6 bytes):
  0x77F10F5E: 48 39 D0     ; cmp rax, rdx
  0x77F10F61: 75 0A        ; jne +10

New (3 bytes + padding):
  0x77F10F5E: 31 C0        ; xor eax, eax
  0x77F10F60: C3           ; ret
  0x77F10F61: (padding)

Result: Function now returns 0x00000000 on every call
```

## 8. Real-World Tool Comparison

```
PCUNLOCKER vs KON-BOOT vs ATTACK

┌────────────────────┬─────────────────┬────────────────────┐
│ ASPECT             │ PCUnlocker      │ Kon-Boot           │
├────────────────────┼─────────────────┼────────────────────┤
│ Bootloader Method  │ UEFI ExitBoot   │ Int 13h Hook       │
│ Compatible Archs   │ UEFI + Legacy   │ Legacy + UEFI      │
│ Persistence        │ Volatile        │ Volatile           │
│ Forensic Artifacts │ None (RAM only) │ None (RAM only)    │
│ EDR Evasion        │ Standard        │ Silent Patching    │
│ Restore on Boot    │ No              │ Yes (restores)     │
│ Detection Rate     │ Medium          │ Low (restoration)  │
│ FDE Bypass         │ No (fails)       │ No (fails)         │
│ VBS/CG Bypass      │ No (fails)       │ No (fails)         │
│ NTLM Target        │ msv1_0.dll      │ msv1_0.dll         │
│ Patch Duration     │ Until reboot    │ Until reboot       │
└────────────────────┴─────────────────┴────────────────────┘


COMPARISON NOTES:
─────────────────
1. Both tools operate on same principle
2. Kon-Boot's "silent patching" is EDR evasion
3. Both fail if BitLocker/VBS enabled
4. Both volatile (important for defense)
5. Real-world usage confirms methodology
```

---

## Summary: Key Takeaways for Paper

1. **Physical Access = Logical Access** (without encryption)
2. **Boot sequence is critical vulnerability point**
3. **Software protections have architectural limits**
4. **Ring 0 code can bypass all user-mode controls**
5. **Only hardware encryption provides true protection**
6. **Commercial tools prove methodology is viable**

---

**Document Status:** Complete visual reference for research paper writing
