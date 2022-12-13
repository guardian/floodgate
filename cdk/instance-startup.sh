#!/bin/bash -ev

adduser --disabled-password content-api

aws configure set region ${AWS::Region}

cd /home/content-api

mkdir -p /etc/gu
mkdir logs

echo 'STAGE=${Stage}' > /etc/gu/install_vars
aws s3 cp s3://content-api-config/content-api-floodgate/${Stage}/floodgate.conf /etc/gu/floodgate.conf
aws s3 cp s3://content-api-dist/${Stack}/${Stage}/content-api-floodgate/content-api-floodgate_1.0_all.deb .

dpkg -i content-api-floodgate_1.0_all.deb

echo JAVA_OPTS=\"-Dpidfile.path=/var/run/content-api-floodgate/floodgate.pid -Dconfig.file=/etc/gu/floodgate.conf\" >> /etc/default/content-api-floodgate

chown -R content-api /home/content-api /etc/gu
chgrp -R content-api /home/content-api /etc/gu

sleep 5s
systemctl restart content-api-floodgate #we need to re-start in order for it to pick up the updated JAVA_OPTS above

ln -s /usr/share/content-api-floodgate floodgate