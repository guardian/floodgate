#!/bin/bash -ev

adduser --disabled-password content-api

aws configure set region ${Region}

cd /home/content-api

mkdir -p /etc/gu
mkdir logs

echo 'STAGE=${Stage}' > /etc/gu/install_vars
aws s3 cp s3://content-api-dist/${Stack}/${Stage}/content-api-floodgate/content-api-floodgate.service /etc/systemd/system/floodgate.service
aws s3 cp s3://content-api-config/content-api-floodgate/${Stage}/floodgate.conf /etc/gu/floodgate.conf
aws s3 cp s3://content-api-dist/${Stack}/${Stage}/content-api-floodgate/content-api-floodgate-${BuiltVersion}.tgz .

tar xfv content-api-floodgate-${BuiltVersion}.tgz
mv content-api-floodgate-${BuiltVersion} floodgate

chown -R content-api /home/content-api /etc/gu
chgrp -R content-api /home/content-api /etc/gu

systemctl start floodgate