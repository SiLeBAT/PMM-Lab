# Auto-generated by EclipseNSIS Script Wizard
# 07.09.2011 11:11:27

Name SiLeBAT-DB

RequestExecutionLevel user

# General Symbol Definitions
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION 1.4.6
!define COMPANY ""
!define URL ""
!define GET_JAVA_URL "http://www.java.com"

# Included files
!include Sections.nsh

# Reserved Files
ReserveFile "${NSISDIR}\Plugins\StartMenu.dll"

# Variables
Var StartMenuGroup

Var JAVA_HOME
Var JAVA_VER
Var JAVA_INSTALLATION_MSG

# Installer pages
Page directory
Page custom StartMenuGroupSelect "" ": $(StartMenuPageTitle)"
Page instfiles

# Installer languages
LoadLanguageFile "${NSISDIR}\Contrib\Language files\German.nlf"

# Installer attributes
OutFile D:\workspaces\SiLeBAT_jars\silebatSetup_${VERSION}.exe
InstallDir $LOCALAPPDATA\SiLeBAT-DB
CRCCheck on
XPStyle on
Icon "${NSISDIR}\Contrib\Graphics\Icons\llama-blue.ico"
ShowInstDetails show
AutoCloseWindow false
VIProductVersion ${VERSION}.0
VIAddVersionKey /LANG=${LANG_GERMAN} ProductName SiLeBAT-DB
VIAddVersionKey /LANG=${LANG_GERMAN} ProductVersion "${VERSION}"
VIAddVersionKey /LANG=${LANG_GERMAN} FileVersion "${VERSION}"
VIAddVersionKey /LANG=${LANG_GERMAN} FileDescription ""
VIAddVersionKey /LANG=${LANG_GERMAN} LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
UninstallIcon "${NSISDIR}\Contrib\Graphics\Icons\classic-uninstall.ico"
ShowUninstDetails show

# Installer sections
Section -Main SEC0000
    Call LocateJVM
    StrCmp "" $JAVA_INSTALLATION_MSG Success OpenBrowserToGetJava
 
    Success:
        SetOutPath $INSTDIR
        SetOverwrite on
        File D:\workspaces\SiLeBAT_jars\SiLeBAT_${VERSION}.jar
        File SiLeBAT.ico
        SetOutPath $SMPROGRAMS\$StartMenuGroup
        CreateShortcut $SMPROGRAMS\$StartMenuGroup\SiLeBAT-DB.lnk '"$JAVA_HOME\bin\javaw.exe"' '-Xms512m -Xmx768M -jar "$INSTDIR\SiLeBAT_${VERSION}.jar"' "$INSTDIR\SiLeBAT.ico" 0
        CreateShortCut $DESKTOP\SiLeBAT-DB.lnk '"$JAVA_HOME\bin\javaw.exe"' '-Xms512m -Xmx768m -jar "$INSTDIR\SiLeBAT_${VERSION}.jar"' "$INSTDIR\SiLeBAT.ico" 0
        WriteRegStr HKLM "${REGKEY}\Components" Main 1
        Goto Done
 
    OpenBrowserToGetJava:
        Exec '"explorer.exe" ${GET_JAVA_URL}'
 
    Done:
SectionEnd

Section -post SEC0001
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    WriteRegStr HKLM "${REGKEY}" StartMenuGroup $StartMenuGroup
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\$(^UninstallLink).lnk" $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o -un.Main UNSEC0000
    Delete /REBOOTOK $DESKTOP\SiLeBAT-DB.lnk
    Delete /REBOOTOK $SMPROGRAMS\$StartMenuGroup\SiLeBAT-DB.lnk
    Delete /REBOOTOK $INSTDIR\SiLeBAT_${VERSION}.jar
    Delete /REBOOTOK $INSTDIR\SiLeBAT.ico
    DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

Section -un.post UNSEC0001
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\$(^UninstallLink).lnk"
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function StartMenuGroupSelect
    Push $R1
    StartMenu::Select /autoadd /text "$(StartMenuPageText)" /lastused $StartMenuGroup SiLeBAT-DB
    Pop $R1
    StrCmp $R1 success success
    StrCmp $R1 cancel done
    MessageBox MB_OK $R1
    Goto done
success:
    Pop $StartMenuGroup
done:
    Pop $R1
FunctionEnd

Function .onInit
    InitPluginsDir
FunctionEnd

# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    ReadRegStr $StartMenuGroup HKLM "${REGKEY}" StartMenuGroup
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd

# Installer Language Strings
# TODO Update the Language Strings with the appropriate translations.

LangString StartMenuPageTitle ${LANG_GERMAN} "Start Menu Folder"

LangString StartMenuPageText ${LANG_GERMAN} "Select the Start Menu folder in which to create the program's shortcuts:"

LangString ^UninstallLink ${LANG_GERMAN} "Uninstall $(^Name)"
 
Function LocateJVM
    ;Check for Java version and location
    Push $0
    Push $1
 
    ReadRegStr $JAVA_VER HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" CurrentVersion
    StrCmp "" "$JAVA_VER" Check64 CheckJavaVer
    
    Check64:
        ;check for 64bit JRE on 64bit 
        SetRegView 64 
        ReadRegStr $JAVA_VER HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion" 
        StrCmp "" "$JAVA_VER" JavaNotPresent CheckJavaVer
      
    JavaNotPresent:
        StrCpy $JAVA_INSTALLATION_MSG "Java Runtime Environment is not installed on your computer. You need version 1.4 or newer to run this program...$JAVA_VER"
        ;MessageBox MB_OK $JAVA_INSTALLATION_MSG
        Goto Done
 
    CheckJavaVer:
        ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$JAVA_VER" JavaHome
        GetFullPathName /SHORT $JAVA_HOME "$0"
        StrCpy $0 $JAVA_VER 1 0
        StrCpy $1 $JAVA_VER 1 2
        StrCpy $JAVA_VER "$0$1"
        IntCmp 14 $JAVA_VER FoundCorrectJavaVer FoundCorrectJavaVer JavaVerNotCorrect
        ;MessageBox MB_OK "Found Java: $JAVA_VER at $JAVA_HOME"
 
    FoundCorrectJavaVer:
        IfFileExists "$JAVA_HOME\bin\javaw.exe" 0 JavaNotPresent
        ;MessageBox MB_OK "Found Java: $JAVA_VER at $JAVA_HOME"
        Goto Done
 
    JavaVerNotCorrect:
        StrCpy $JAVA_INSTALLATION_MSG "The version of Java Runtime Environment installed on your computer is $JAVA_VER. Version 1.4 or newer is required to run this program."
        ;MessageBox MB_OK $JAVA_INSTALLATION_MSG 
    Done:
        Pop $1
        Pop $0
FunctionEnd