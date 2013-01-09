#!/bin/sh

if [ "${1}" = "" ] ; then
	echo "Usage: "`basename ${0}`" CLASSNAME [arguments...]"
	exit 0
fi
ss_class=${1}
shift

# StreamSpinner�̃g�b�v�f�B���N�g���ֈړ�
cd `dirname $0`/..
ss_home=.

# �����ݒ�
ss_classes=${ss_home}/bin
ss_lib=${ss_home}/lib
ss_policy=${ss_classes}/StreamSpinner.policy

# �N���X�p�X�̋�؂蕶�����w��
delim=":"
if [ `uname` = "CYGWIN_NT-6.1" ]; then
	delim=";"
fi

# ���s�ɕK�v�ȃ��C�u�������N���X�p�X�ɒǉ�
export CLASSPATH="${CLASSPATH}${delim}${ss_classes}"
for i in ${ss_lib}/*.jar ; do
	export CLASSPATH="${CLASSPATH}${delim}${i}"
done

java -Xmx256m -Djava.security.policy=${ss_policy} -Djava.rmi.server.codebase=file:///${ss_classes} ${ss_class} $@
