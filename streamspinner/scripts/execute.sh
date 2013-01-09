#!/bin/sh

if [ "${1}" = "" ] ; then
	echo "Usage: "`basename ${0}`" CLASSNAME [arguments...]"
	exit 0
fi
ss_class=${1}
shift

# StreamSpinnerのトップディレクトリへ移動
cd `dirname $0`/..
ss_home=.

# 初期設定
ss_classes=${ss_home}/bin
ss_lib=${ss_home}/lib
ss_policy=${ss_classes}/StreamSpinner.policy

# クラスパスの区切り文字を指定
delim=":"
if [ `uname` = "CYGWIN_NT-6.1" ]; then
	delim=";"
fi

# 実行に必要なライブラリをクラスパスに追加
export CLASSPATH="${CLASSPATH}${delim}${ss_classes}"
for i in ${ss_lib}/*.jar ; do
	export CLASSPATH="${CLASSPATH}${delim}${i}"
done

java -Xmx256m -Djava.security.policy=${ss_policy} -Djava.rmi.server.codebase=file:///${ss_classes} ${ss_class} $@
