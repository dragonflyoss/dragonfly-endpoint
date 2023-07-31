# dragonfly-endpoint

The TorchServe endpoint that downloads model via Dragonfly.

## Getting Started

1. Install Troch Serve.

    Refer to the documentation [here](https://github.com/pytorch/serve).

2. Install Dragonfly plugin jar.

    `wget https://github.com/dragonflyoss/dragonfly-endpoint/add_basic_framework/Dragonfly.jar`

2. Initial Dragonfly plugin.

    Note: To run Dragonfly plugins on Torch Serve, Python>=3.8, Java>=11 is required.

    There are following two ways to include Dragonfly plugin jars to Torch Serve.

    - Using config. property: Add following line to your Torch Serve config. properties file. `plugins_path=<path-containing-plugin-jars">`.

    - Using command line option:  `torchserve --start --model-store <your-model-store-path> --plugins-path=<path-to-plugin-jars>`.

## Set dragonfly_endpoint.json file

The Torch Serve endpoint now support: Amazon Web Services(AWS), Google Cloud Platform(GCS), Azure Blob Storage(ABS), Alibaba Cloud(OSS).

To use these object storage serves, users need creat `dragonfly_endpoint.json` file and set the config of serve and Dragonfly in it.

### Config variables

1. Dragonfly
    * `addr` - Address of Dragonfly, values save in a list.
    * `header` - Header of  Dragonfly, values save in a map.


2. Object storage
    * AWS S3
        * `access_key`
        * `secret_key`
        * `region`
        * `bucket_name`

    * GCS

        * `project_id`
        * `serviceAccountPath`
        * `bucket_name`

    * ABS

        * `account_name`
        * `account_key`
        * `endpoint`
        * `container_name`

    * OSS
        * `endpoint`
        * `access_key_id`
        * `access_key_secret`
        * `bucket_name`


```bash
{
  "addr": "http://127.0.0.1:65001",
  "header": {
    "Accept": "*",
    "Host": "abc"
  },
  "filter": [
    "key",
    "sign"
  ],
  "object_storage": {
    "type": "s3",
    "bucket_name": "pytorch-model",
    "region": "us-east-1",
    "access_key": "lwIImYkqPuLkTIRe2Jkl",
    "secret_key": "T6M1hChdHPMxei4VeVNOhj1zGL6N193LCdnD9GGE"
  }
}

```

### Config path

dragonfly_endpoint.json has default file path.

- Windows: `C:\\ProgramData\\dragonfly_endpoint\\dragonfly_endpoint.json`
- Linux:`/etc/dragonfly_endpoint/dragonfly_endpoint.json`
- MacOS:`~/.dragonfly_endpoint/dragonfly_endpoint.json`

Users can also set file path personally via environment variables.

` export DRAGONFLY_ENDPOINT_CONFIG=<path-to-dragonfly_endpoint.json>`

## Use Torch Serve Endpoint

Users can use Dragonfly endpoint of Torch Serve Manage API to download and register model file.

```bash
curl -X POST  "http://localhost:8081/dragonfly?filename=squeezenet_v1.1.mar"

{
  "status": "Model \"squeezenet_v1.1\" Version: 1.0 registered with 0 initial workers. Use scale workers API to add workers for the model."
}
```
