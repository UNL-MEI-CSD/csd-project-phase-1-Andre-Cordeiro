
NBR = $(nbr)


PORTS = $(shell seq 5000 2 $$((($(NBR)-1)*2+5000)))

MEMBERSHIP = $(shell echo $(PORTS) | sed 's/ /,localhost:/g' | sed 's/^/localhost:/')



all: kill clean
	
	@echo "${BOLD}${SYELLOW}📦  Building the jar file ${S}"
	@mvn package
	@echo "${BOLD}${SGREEN}Jar file built 👍"
	
	@echo "${BOLD}${SCYAN}Number of nodes: $(NBR) 🤖"
	@echo "${BOLD}${SCYAN}Starting the nodes 🚀 "
	@echo ""

	@for number in $(shell seq 1 $(NBR)); do \
		echo "${BOLD}${SGREEN}Starting node n°$$number on port $$((5000+($$number-1)*2)) ${S}"; \
		echo ""; \
		gnome-terminal  --working-directory=$(PWD)/deploy -- bash -c "echo '🚀 Starting node n°$$number on port $$((5000+($$number-1)*2)) :'; \
		java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=$$((5000+($$number-1)*2)) initial_membership=$(MEMBERSHIP) crypto_name=node$$number; \
        exec bash;" ;\
	done

	@echo "${BOLD}${SBLUE}🎉  All nodes are started ${S}"
	

clean:
	@echo "${SPURPLE}🧹  Cleaning the project ${S}"
	@mvn clean
	@echo "${SPURPLE}Project cleaned 👍 ${S}"


kill:
	@echo "${BOLD}${SYELLOW}🔪  Killing all java processes ${S}"
	@for pid in $(shell ps -ef | grep java | grep -v grep | awk '{print $$2}'); do \
		kill -9 $$pid; \
	done
	@echo "${BOLD}${SYELLOW}All java processes/terminal killed 👍 ${S}"

stop:
#same but with a emoji stop sign instead 
	@echo "${BOLD}${SORANGE}🛑  Stopping all java processes ${S}"
	@for pid in $(shell ps -ef | grep java | grep -v grep | awk '{print $$2}'); do \
		kill -2 $$pid; \
	done
	@echo "${BOLD}${SORANGE}All java processes stop 👍 ${S}"
	






	


# Font
S 		=		\033[0m
BOLD 	= 		\033[1m

# Colors

SGREEN	=		\033[92m
SYELLOW	=		\033[93m
SBLUE	=		\033[94m
SPURPLE	=		\033[95m
SCYAN	=		\033[96m
SORANGE = 		\033[33m