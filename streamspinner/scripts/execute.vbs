rem �����̊m�F
Set args = WScript.Arguments
If args.Count <> 1 Then
	WScript.Echo "Usage: " & WScript.ScriptFullName & " CLASSNAME"
	WScript.Quit
End If
STREAMSPINNER_CLASS = args(0)

rem �����ݒ�
Set shell = WScript.CreateObject("WScript.Shell")
Set env = shell.Environment("PROCESS")
Set fso = WScript.CreateObject("Scripting.FileSystemObject")
Set re = new RegExp
re.Pattern = ".*\.jar"

shell.CurrentDirectory = fso.GetParentFolderName(WScript.ScriptFullName) & "\.."

rem ���s�ɕK�v�ȃ��C�u�������N���X�p�X�ɒǉ�
STREAMSPINNER_HOME = "."
STREAMSPINNER_CLASSES = STREAMSPINNER_HOME & "\bin"
STREAMSPINNER_LIB = STREAMSPINNER_HOME & "\lib"
STREAMSPINNER_POLICY = STREAMSPINNER_CLASSES & "\StreamSpinner.policy"

CLASSPATH = env("CLASSPATH")
CLASSPATH = CLASSPATH & ";" & STREAMSPINNER_CLASSES

rem lib�ȉ��ɒu����Ă���jar�t�@�C���ւ̃N���X�p�X�ݒ�
Set fd = fso.GetFolder(STREAMSPINNER_LIB)
Set fs = fd.Files
For Each f in fs
	If re.Test(f) Then
		CLASSPATH = CLASSPATH & ";" & f
	End If
Next

env("CLASSPATH") = CLASSPATH

shell.Run "java -Xmx256m -Djava.security.policy=" & STREAMSPINNER_POLICY & " -Djava.rmi.server.codebase=file:///" & STREAMSPINNER_CLASSES & " " & STREAMSPINNER_CLASS, 6, False
