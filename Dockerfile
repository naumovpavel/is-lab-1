FROM quay.io/wildfly/wildfly:33.0.1.Final-jdk17

WORKDIR /opt/jboss/wildfly

COPY build/libs/*.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080
EXPOSE 9990

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
