rem 起動に使うJavaのクラス名
STREAMSPINNER_CLASS = "org.streamspinner.system.CQTerminalCUI"

Set shell = WScript.CreateObject("WScript.Shell")
shell.Run "execute.vbs " & STREAMSPINNER_CLASS, 0, False
