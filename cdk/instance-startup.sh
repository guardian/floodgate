#!/bin/bash -ev

adduser --disabled-password content-api

aws configure set region ${Region}

cd /home/content-api

mkdir -p /etc/gu
mkdir logs

echo 'STAGE=${Stage}' > /etc/gu/install_vars
aws s3 cp s3://content-api-config/content-api-floodgate/${Stage}/floodgate.conf /etc/gu/floodgate.conf
aws s3 cp s3://content-api-dist/${Stack}/${Stage}/content-api-floodgate/floodgate_1.0_all.deb .

dpkg -i floodgate_1.0_all.deb

echo JAVA_OPTS=\"-Dpidfile.path=/var/run/floodgate/floodgate.pid -Dconfig.file=/etc/gu/floodgate.conf\" >> /etc/default/floodgate

chown -R content-api /home/content-api /etc/gu
chgrp -R content-api /home/content-api /etc/gu

systemctl start floodgate

ln -s /usr/share/floodgate floodgate