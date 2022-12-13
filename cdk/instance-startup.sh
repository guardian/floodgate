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

systemctl start content-api-floodgate

ln -s /usr/share/content-api-floodgate floodgate

export HOSTNAME=$(hostname)
cat > /etc/gu/otel.yaml << EOF
extensions:
  sigv4auth:
    service: "aps"  #APS refers to "Amazon Prometheus Service" aka Amazon Managed Prometheus aka AMP
    region: "${AWS::Region}"

receivers:
  prometheus:
    config:
      scrape_configs:
        - job_name: 'floodgate'
          scrape_interval: 120s
          static_configs:
            # metrics endpoint for your app
            - targets: [ '0.0.0.0:9000' ]
              labels:
                instance: ${HOSTNAME}
                stack: ${Stack}
                stage: ${Stage}
processors:
  batch/metrics:
    timeout: 120s

exporters:
  prometheusremotewrite:
    endpoint: "${PrometheusRemoteWriteUrl}"
    auth:
      authenticator: sigv4auth

service:
  extensions: [sigv4auth]
  pipelines:
    metrics:
      receivers: [prometheus]
      processors: [batch/metrics]
      exporters: [prometheusremotewrite]
EOF
/opt/aws/aws-otel-collector/bin/aws-otel-collector-ctl -c /etc/gu/otel.yaml -a start