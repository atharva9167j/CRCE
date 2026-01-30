# Reference List: Runtime Authentication Subversion via Ephemeral Kernel-Mode Patching

## Primary References (Academic & Research Papers)

### Bootkit and Pre-OS Execution
[1] Kallenberg, C., Kovah, X., Butterworth, J., & Cornwell, S. (2014). "Extreme Privilege Escalation on Windows 8 UEFI Systems." *Black Hat USA*. Available: https://blackhat.com/docs/us-14/materials/us-14-Kallenberg-Extreme-Privilege-Escalation-On-Windows8-UEFI-Systems.pdf

[2] Lindsay, J., & Hoglund, G. (2007). "Attacking the Windows Kernel." *Black Hat USA*. Available: https://blackhat.com/presentations/bh-usa-07/Lindsay/Whitepaper/bh-usa-07-lindsay-WP.pdf

[3] Wardle, P. (2015). "The Art of Mac Malware: Crafting Sophisticated Attacks." *Security Summit*. (Referenced for bootkit architecture principles applicable to x86/x64 systems)

### Driver Signature Enforcement & Code Integrity Bypass
[4] SafeBreach Security Research Team. (2025). "A Zero-Restart Kernel Bypass: Exploiting Code Integrity Callback Mechanisms." *Cyber Security Research*. Available: https://www.linkedin.com/pulse/zero-restart-kernel-bypass-exploiting-code-integrity-callback-doli-nt5qe

[5] Fortinet FortiGuard Labs. (2022). "The Swan Song for Driver Signature Enforcement Tampering." *Fortinet Blog*. Available: https://www.fortinet.com/blog/threat-research/driver-signature-enforcement-tampering

[6] BleepingComputer Security News. (2024). "New Windows Driver Signature Bypass Allows Kernel Rootkit Installs." Available: https://www.bleepingcomputer.com/news/security/new-windows-driver-signature-bypass-allows-kernel-rootkit-installs/

### Kernel Patch Protection (PatchGuard) Evasion
[7] Fortinet Threat Research. (2018). "Melting Down PatchGuard: Leveraging KPTI to Bypass Kernel Patch Protection." Available: https://www.fortinet.com/blog/threat-research/melting-down-patchguard-leveraging-kpi-to-bypass-kernel-patch-protection

[8] Hackyboiz Research Team. (2024). "Bypassing Windows Kernel Mitigations: Part 1." Available: https://hackyboiz.github.io/2024/12/08/l0ch/bypassing-kernel-mitigation-part1/en/

[9] Hackyboiz Research Team. (2025). "Bypassing Windows Kernel Mitigations: Part 2." Available: https://hackyboiz.github.io/2025/01/12/l0ch/bypassing-kernel-mitigation-part2/en/

### Process Context Switching & Memory Access
[10] Ciholas, P., Kalisch, M., & Kuncak, V. (2020). "Fast and Furious: Outrunning Windows Kernel Notification Routines from User-Mode." *PMC National Center for Biotechnology Information*. Available: https://pmc.ncbi.nlm.nih.gov/articles/PMC7338165/

[11] Korkin, I. (2021). "Windows Kernel Hijacking Is Not An Option." *arXiv*. Available: https://arxiv.org/pdf/2106.06065.pdf

[12] SafeBreach Labs. (2024). "Process Injection Using Windows Thread Pools: Pool Party Attacks." Available: https://www.safebreach.com/blog/process-injection-using-windows-thread-pools/

### NTLM Authentication & Subversion
[13] Checkpoint Research. (2025). "CVE-2025-24054: NTLM Exploit in the Wild." Available: https://research.checkpoint.com/2025/cve-2025-24054-ntlm-exploit-in-the-wild/

[14] Cymulate Security. (2025). "CVE-2025-50154: Zero Click, One NTLM: Patch Bypass." Available: https://cymulate.com/blog/zero-click-one-ntlm-microsoft-security-patch-bypass-cve-2025-50154/

[15] Unit42 Palo Alto Networks. (2024). "Threat Brief: Microsoft Critical Vulnerabilities (CVE-2022-26925)." Available: https://unit42.paloaltonetworks.com/microsoft-cve-2022-26925-etc/

### LSASS Memory & Credential Access
[16] MITRE ATT&CK Framework. (2025). "OS Credential Dumping: LSASS Memory." Available: https://attack.mitre.org/techniques/T1003/001/

[17] JumpCloud Security. (2025). "What is an LSASS Memory Dump?" Available: https://jumpcloud.com/it-index/what-is-an-lsass-memory-dump

[18] Elastic Security. (2024). "Credential Acquisition via Registry Hive Dumping." Available: https://www.elastic.co/guide/en/security/8.19/credential-acquisition-via-registry-hive-dumping.html

### Win32k & Kernel Object Manipulation
[19] NCC Group Security Research. (2021). "CVE-2021-31956: Exploiting the Windows Kernel (NTFS with WNF)." Available: https://www.nccgroup.com/research-blog/cve-2021-31956-exploiting-the-windows-kernel-ntfs-with-wnf-part-1/

[20] Safe Security. (2023). "Windows Win32k Elevation of Privilege Vulnerability." Available: https://safe.security/wp-content/uploads/windows-win32k-elevation-of-privilege-vulnerability.pdf

### Kernel Memory Protection & Virtualization-Based Security
[21] Korkin, I. (2018). "MemoryRanger: Protecting Drivers from Kernel Attacks via Hypervisor-Based Monitoring." *arXiv*. Available: https://arxiv.org/pdf/1812.09920.pdf

[22] Srivastava, A., Lanzi, A., & Mitchell, J. C. (2017). "Gateway: Monitoring Untrusted Kernel-Mode Execution." *NDSS Symposium*. Available: https://www.ndss-symposium.org/wp-content/uploads/2017/09/sriv.pdf

[23] Microsoft Security Research Center. (2023). "Understanding VBS and Credential Guard: Virtualization-Based Security." (Referenced for VTL architecture and LSASS protection)

### SMM/Firmware Exploitation
[24] Kalenberg, C., Kovah, X., & Butterworth, J. (2014). "Extreme Privilege Escalation on Windows 8 UEFI Systems: SMM/Ring 0 Compromise." *Black Hat USA Whitepaper*. Available: https://blackhat.com/docs/us-14/materials/us-14-Kallenberg-Extreme-Privilege-Escalation-On-Windows8-UEFI-Systems-WP.pdf

### Contemporary Kernel Vulnerabilities & Exploits
[25] Wiz.io Security Research. (2025). "CVE-2025-62215: Windows Kernel Race Condition Impact & Mitigation." Available: https://www.wiz.io/vulnerability-database/cve/cve-2025-62215

[26] SocPrime Threat Research. (2025). "CVE-2025-62215: Microsoft Patches Windows Kernel Zero-Day." Available: https://socprime.com/blog/latest-threats/cve-2025-62215-windows-kernel-vulnerability/

[27] Microsoft Security Blog. (2025). "Exploitation of CLFS Zero-Day (CVE-2025-29824) Leads to Ransomware Activity." Available: https://www.microsoft.com/en-us/security/blog/2025/04/08/exploitation-of-clfs-zero-day-leads-to-ransomware-activity/

### Kernel Exploitation Techniques & Tutorials
[28] McGarr, C. (2020). "Token Stealing Payloads Revisited on Windows 10 x64: x64 Kernel Shellcode and SMEP Bypass." Available: https://connormcgarr.github.io/x64-Kernel-Shellcode-Revisited-and-SMEP-Bypass/

[29] XPNSec Security Research. (2018). "Exploiting Windows 10 Kernel Drivers: Stack Overflow." Available: https://blog.xpnsec.com/hevd-stack-overflow/

[30] Fluid Attacks Security Research. (2025). "HEVD: kASLR + SMEP Bypass Demonstration." Available: https://fluidattacks.com/blog/hevd-smep-bypass

### KASLR Bypass Techniques
[31] OffSec Security. (2020). "Development of a New Windows 10 KASLR Bypass (in One WinDbg Command)." Available: https://www.offsec.com/blog/development-of-a-new-windows-10-kaslr-bypass-in-one-windbg-command/

[32] Core Security. (2020). "Windows SMEP Bypass." Available: https://www.coresecurity.com/sites/default/files/2020-06/Windows%20SMEP%20bypass%20U%20equals%20S_0.pdf

### Kerberos & Authentication Protocol Vulnerabilities
[33] Silverfort Security Research. (2025). "CVE-2025-60704: Windows Kerberos CheckSum Vulnerability - Critical Domain Compromise." (Referenced for Kerberos constrained delegation bypass)

[34] Microsoft Security Updates. (2025). "CVE-2024-20674: Windows Kerberos Security Feature Bypass - Critical Rating." (Referenced for Kerberos ticket manipulation)

[35] Microsoft Security Bulletin. (2016). "MS16-014: Important Security Update." Available: https://learn.microsoft.com/en-us/security-updates/securitybulletins/2016/ms16-014

### LSA & Active Directory Authentication Bypass
[36] JumpCloud Security. (2025). "What is the Local Security Authority (LSA)?" Available: https://jumpcloud.com/it-index/what-is-the-local-security-authority-lsa

[37] Lepide Security. (2025). "Why You Should Enable LSA Protection: Protected Process Light (PPL) Mechanisms." Available: https://www.lepide.com/blog/why-you-should-enable-lsa-protection/

[38] Rackspace Security Documentation. (2022). "Windows LSA Spoofing Vulnerability CVE-2022-26925." Available: https://docs.rackspace.com/docs/windows-lsa-spoofing-vulnerability-cve-2022-26925

[39] Team Hydra Security Research. (2020). "Bypassing Credential Guard: Memory Patching Techniques." Available: https://teamhydra.blog/2020/08/25/bypassing-credential-guard/

[40] Elastic Security. (2025). "How Attackers Abuse Access Token Manipulation (ATT&CK T1134)." Available: https://www.elastic.co/blog/how-attackers-abuse-access-token-manipulation

### GDI Object Abuse & Kernel Exploitation
[41] El-Sherei, S. (2017). "Demystifying Windows Kernel Exploitation." *DEF CON 25*. Available: https://www.youtube.com/watch?v=2chDv_wTymc

### Memory Corruption & Use-After-Free
[42] Google Security Research. (2013). "Identifying and Exploiting Windows Kernel Race Conditions." Available: https://research.google.com/pubs/archive/42189.pdf

[43] SocPrime Threat Intelligence. (2025). "CVE-2024-1086: Critical Privilege Escalation in Kernel Memory Management." Available: https://socprime.com/blog/cve-2024-1086-vulnerability/

### Commercial Tools & Implementations
[44] PCUnlocker Development Team. (2024). "PCUnlocker: UEFI ExitBootServices Hook Implementation." (Referenced for real-world UEFI patching mechanisms)

[45] Kon-Boot Development Team. (2024). "Kon-Boot: Legacy Int 13h Hook & UEFI Shim Implementation." (Referenced for MBR-based bootkit injection techniques)

### Defense & Detection
[46] CrowdStrike Falcon Labs. (2024). "CrowdStrike Falcon Prevents Vulnerable Driver Attacks." Available: https://www.crowdstrike.com/en-us/blog/falcon-prevents-vulnerable-driver-attacks-real-world-intrusion/

[47] Red Canary Threat Detection. (2025). "OS Credential Dumping - Threat Detection Report & Techniques." Available: https://redcanary.com/threat-detection-report/techniques/os-credential-dumping/

[48] Trend Micro Security Research. (2020). "An In-Depth Look at Windows Kernel Threats." Available: https://documents.trendmicro.com/assets/white_papers/wp-an-in-depth-look-at-windows-kernel-threats.pdf

[49] Apriorit Security. (2025). "Mitigate DLL Injection Attacks: Defense Mechanisms." Available: https://www.apriorit.com/dev-blog/secure-windows-software-against-dll-attacks

[50] SentinelOne Labs. (2021). "Case Study: Why You Shouldn't Trust NTDLL from Kernel Image Load Callbacks." Available: https://www.sentinelone.com/labs/case-study-why-you-shouldnt-trust-ntdll-from-kernel-image-load-callbacks/

## Secondary References (CVE Details & Security Advisories)

[51] CVE Details Database. (2025). "CVE-2025-62215: Windows Kernel Elevation of Privilege." Available: https://www.cvedetails.com/cve/CVE-2025-62215/

[52] Fortiguard IPS Encyclopedia. (2025). "MS.Windows.CVE-2025-62215.Privilege.Elevation." Available: https://fortiguard.fortinet.com/encyclopedia/ips/59383

[53] HelpNetSecurity. (2025). "Patch Tuesday Microsoft CVE-2025-62215." Available: https://www.helpnetsecurity.com/2025/11/12/patch-tuesday-microsoft-cve-2025-62215/

[54] TheHackerNews. (2025). "Microsoft Fixes 63 Security Flaws, Including Windows Kernel Zero-Day." Available: https://thehackernews.com/2025/11/microsoft-fixes-63-security-flaws.html

[55] CVE Details. (2025). "MS13-053: Vulnerabilities in Windows Kernel-Mode Drivers." Available: https://www.cvedetails.com/microsoft-bulletin/MS13-053/

[56] CVE Details. (2025). "MS15-051: Vulnerabilities in Windows Kernel-Mode Drivers Could Allow RCE." Available: https://www.cvedetails.com/microsoft-bulletin/MS15-051/

[57] CVE Details. (2025). "MS13-101: Vulnerabilities in Windows Kernel-Mode Drivers." Available: https://www.cvedetails.com/microsoft-bulletin/MS13-101/

## Tertiary References (Educational & Technical Analysis)

[58] HackingArticles. (2025). "Windows Privilege Escalation: Kernel Exploit." Available: https://www.hackingarticles.in/windows-privilege-escalation-kernel-exploit/

[59] HackingArticles. (2025). "Credential Dumping: SAM." Available: https://www.hackingarticles.in/credential-dumping-sam/

[60] InfoSecWriteups. (2022). "Escalation of Windows Privilege: Kernel Exploit." Available: https://infosecwriteups.com/escalation-of-windows-privilege-kernel-exploit-bddda00c1ab2

[61] TroventIO Security. (2025). "Breaking Into the Windows Kernel: A Deep Dive into Exploitation." Available: https://trovent.io/en/windows-kernel-exploitation/

[62] NetworkIntelligence AI. (2025). "Windows Kernel Exploitation." Available: https://www.networkintelligence.ai/blogs/windows-kernel-exploitation/

[63] Compass Security. (2025). "Windows Access Tokens: From Authentication to Exploitation." Available: https://www.compass-security.com/fileadmin/Research/Presentations/2025_07_Windows_Access_Tokens_From_Authentication_To_Exploitat...

[64] EvsinC33 Security Blog. (2022). "Windows Access Tokens: Getting SYSTEM and Demystifying Potato Exploits." Available: https://eversinc33.com/2022/11/25/windows-access-tokens-getting-system-and-demystifying-potato-exploits

[65] Snyk Security Blog. (2020). "Kernel Privilege Escalation." Available: https://snyk.io/blog/kernel-privilege-escalation/

[66] HelpNetSecurity. (2025). "Immersive Labs: Patch Tuesday November 2025 Analysis." Available: https://www.immersivelabs.com/resources/c7-blog/patch-tuesday-november-2025---critical-microsoft-security-patches-released-for-p...

[67] Picus Security. (2020). "MITRE ATT&CK T1003: Credential Dumping." Available: https://www.picussecurity.com/resource/blog/picus-10-critical-mitre-attck-techniques-t1003-credential-dumping

[68] Picus Security. (2025). "Microsoft Active Directory Domain Services CVE-2025-21293 Vulnerability Explained." Available: https://www.picussecurity.com/resource/blog/microsoft-active-directory-domain-services-cve-2025-21293-vulnerability-explained

[69] CyberSRCC. (2025). "Privilege Escalation Flaw Found in Windows Active Directory Domain Services." Available: https://cybersrcc.com/2025/04/11/privilege-escalation-flaw-found-in-windows-active-directory-domain-services/

[70] Fortinet Threat Research. (2020). "CVE-2020-0796: Memory Corruption Vulnerability in Windows 10 SMB Server." Available: https://www.fortinet.com/blog/threat-research/cve-2020-0796-memory-corruption-vulnerability-in-windows-10-smb-server

[71] Security.com Threat Intelligence. (2025). "Ransomware Attackers Leveraged Privilege Escalation Zero-Day." Available: https://www.security.com/threat-intelligence/play-ransomware-zero-day

---

## References Organization by Topic

### Bootkit & UEFI/BIOS Mechanisms
References: [1], [2], [24]

### Driver Signature Enforcement (DSE) Bypass
References: [4], [5], [6]

### Kernel Patch Protection (PatchGuard) Evasion
References: [7], [8], [9]

### Process Memory Access & Context Switching
References: [10], [12], [40]

### NTLM Authentication Vulnerability
References: [13], [14], [15]

### LSASS & Credential Access
References: [16], [17], [18], [47]

### Win32k & Kernel Objects
References: [19], [20]

### Memory Protection & Virtualization
References: [21], [22], [23]

### GDI Exploitation & Kernel Shellcode
References: [28], [29], [30], [41]

### KASLR & Memory Protection Bypass
References: [31], [32]

### Kerberos Authentication Bypass
References: [33], [34], [35]

### LSA Protection & Credential Guard
References: [36], [37], [38], [39]

### Race Conditions & Memory Corruption
References: [42], [43]

### Real-World Tools & Implementations
References: [44], [45]

### Detection & Defense
References: [46], [47], [48], [49], [50]

---

**Total References:** 71 sources covering academic papers, security advisories, technical blogs, and commercial tool analysis.
