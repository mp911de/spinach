PATH := ./work/disque-git/src:${PATH}
ROOT_DIR := $(shell pwd)
STUNNEL_BIN := $(shell which stunnel)
BREW_BIN := $(shell which brew)
YUM_BIN := $(shell which yum)
APT_BIN := $(shell which apt-get)

#######
# Disque
#######
.PRECIOUS: work/disque-%/disque.conf

work/disque-%/disque.conf:
	@mkdir -p $(@D)

	@echo port $* >> $@
	@echo daemonize yes >> $@
	@echo pidfile $(ROOT_DIR)/work/disque-$*/disque.pid >> $@
	@echo logfile $(ROOT_DIR)/work/disque-$*/disque.log >> $@
	@echo appendonly no >> $@
	@echo unixsocket $(ROOT_DIR)/work/disque-$*/socket >> $@
	@echo unixsocketperm 777 >> $@

work/disque-%/disque.pid: work/disque-%/disque.conf work/disque-git/src/disque-server
	cd work/disque-$* && ../../work/disque-git/src/disque-server disque.conf

disque-start: work/disque-7711/disque.pid  work/disque-7712/disque.pid  work/disque-7713/disque.pid

disque-init: disque-start work/disque-git/src/disque-server
	work/disque-git/src/disque cluster meet 127.0.0.1 7712
	work/disque-git/src/disque cluster meet 127.0.0.1 7713


##########
# stunnel
##########

work/stunnel.conf:
	@mkdir -p $(@D)

	@echo cert=$(ROOT_DIR)/work/cert.pem >> $@
	@echo key=$(ROOT_DIR)/work/key.pem >> $@
	@echo capath=$(ROOT_DIR)/work/cert.pem >> $@
	@echo cafile=$(ROOT_DIR)/work/cert.pem >> $@
	@echo delay=yes >> $@
	@echo pid=$(ROOT_DIR)/work/stunnel.pid >> $@
	@echo foreground = no >> $@

	@echo [stunnel] >> $@
	@echo accept = 127.0.0.1:7443 >> $@
	@echo connect = 127.0.0.1:7711 >> $@

work/stunnel.pid: work/stunnel.conf ssl-keys
	which stunnel4 >/dev/null 2>&1 && stunnel4 $(ROOT_DIR)/work/stunnel.conf || stunnel $(ROOT_DIR)/work/stunnel.conf

stunnel-start: work/stunnel.pid

start: cleanup
	$(MAKE) disque-init
	$(MAKE) stunnel-start


cleanup: stop
	@mkdir -p $(@D)
	rm -f work/*.rdb work/*.aof work/*.conf work/*.log 2>/dev/null
	rm -f *.aof
	rm -f *.rdb

##########
# SSL Keys
#  - remove Java keystore as becomes stale
##########
work/key.pem work/cert.pem:
	@mkdir -p $(@D)
	openssl genrsa -out work/key.pem 4096
	openssl req -new -x509 -key work/key.pem -out work/cert.pem -days 365 -subj "/O=lettuce/ST=Some-State/C=DE/CN=lettuce-test"
	chmod go-rwx work/key.pem
	chmod go-rwx work/cert.pem
	- rm -f work/keystore.jks

work/keystore.jks:
	@mkdir -p $(@D)
	$$JAVA_HOME/bin/keytool -importcert -keystore work/keystore.jks -file work/cert.pem -noprompt -storepass changeit

ssl-keys: work/key.pem work/cert.pem work/keystore.jks

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
work/disque-git/src/disque work/disque-git/src/disque-server:
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
