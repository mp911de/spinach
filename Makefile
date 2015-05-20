PATH := ./work/disque-git/src:${PATH}
ROOT_DIR := $(shell pwd)
STUNNEL_BIN := $(shell which stunnel)
BREW_BIN := $(shell which brew)
YUM_BIN := $(shell which yum)
APT_BIN := $(shell which apt-get)

define DISQUE1_CONF
daemonize yes
port 7711
pidfile disque.pid
logfile disque.log

appendonly no
unixsocket $(ROOT_DIR)/work/disque-7711/socket
unixsocketperm 777
endef

define DISQUE2_CONF
daemonize yes
port 7712
pidfile disque.pid
logfile disque.log
appendonly no
unixsocket $(ROOT_DIR)/work/disque-7712/socket
unixsocketperm 777
endef

define STUNNEL_CONF
cert=$(ROOT_DIR)/work/cert.pem
key=$(ROOT_DIR)/work/key.pem
capath=$(ROOT_DIR)/work/cert.pem
cafile=$(ROOT_DIR)/work/cert.pem
delay=yes
pid=$(ROOT_DIR)/work/stunnel.pid
foreground = no

[stunnel]
accept = 127.0.0.1:7443
connect = 127.0.0.1:7711

endef

export DISQUE1_CONF
export DISQUE2_CONF

export STUNNEL_CONF

start: cleanup
	mkdir -p work/disque-7711
	mkdir -p work/disque-7712
	echo "$$DISQUE1_CONF" > work/disque-7711/disque.conf && cd work/disque-7711 && ../disque-git/src/disque-server disque.conf
	echo "$$DISQUE2_CONF" > work/disque-7712/disque.conf && cd work/disque-7712 && ../disque-git/src/disque-server disque.conf
	echo "$$STUNNEL_CONF" > work/stunnel.conf
	which stunnel4 >/dev/null 2>&1 && stunnel4 work/stunnel.conf || stunnel work/stunnel.conf
	work/disque-git/src/disque cluster meet 127.0.0.1 7712


cleanup: stop
	- mkdir -p work
	rm -f work/*.rdb work/*.aof work/*.conf work/*.log 2>/dev/null
	rm -f *.aof
	rm -f *.rdb

ssl-keys:
	- mkdir -p work
	- rm -f work/keystore.jks
	openssl genrsa -out work/key.pem 4096
	openssl req -new -x509 -key work/key.pem -out work/cert.pem -days 365 -subj "/O=disque/ST=Some-State/C=DE/CN=spinach-test"
	chmod go-rwx work/key.pem
	chmod go-rwx work/cert.pem
	$$JAVA_HOME/bin/keytool -importcert -keystore work/keystore.jks -file work/cert.pem -noprompt -storepass changeit

stop:
	pkill stunnel || true
	pkill disque-server && sleep 1 || true

test-coveralls:
	make start
	mvn -B -DskipTests=false clean compile test jacoco:report coveralls:report
	make stop

test: start
	mvn -B -DskipTests=false clean compile test
	make stop

prepare: stop

ifndef STUNNEL_BIN
ifeq ($(shell uname -s),Linux)
ifdef APT_BIN
	sudo apt-get install -y stunnel
else

ifdef YUM_BIN
	sudo yum install stunnel
else
	@echo "Cannot install stunnel using yum/apt-get"
	@exit 1
endif

endif

endif

ifeq ($(shell uname -s),Darwin)

ifndef BREW_BIN
	@echo "Cannot install stunnel because missing brew.sh"
	@exit 1
endif

	brew install stunnel

endif

endif
	[ ! -e work/disque-git ] && git clone https://github.com/antirez/disque.git work/disque-git && cd work/disque-git|| true
	[ -e work/disque-git ] && cd work/disque-git && git reset --hard && git pull || true
	make -C work/disque-git clean
	make -C work/disque-git -j4

clean:
	rm -Rf work/
	rm -Rf target/

release:
	mvn release:clean
	mvn release:prepare -Psonatype-oss-release
	mvn release:perform -Psonatype-oss-release
	ls target/checkout/target/*-bin.zip | xargs gpg -b -a
	ls target/checkout/target/*-bin.tar.gz | xargs gpg -b -a
	cd target/checkout && mvn site:site && mvn -o scm-publish:publish-scm -Dgithub.site.upload.skip=false

