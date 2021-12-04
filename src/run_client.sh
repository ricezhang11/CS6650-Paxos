CLIENT_IMAGE='myclient'
PROJECT_NETWORK='mynetwork'

if [ $# -ne 1 ]
then
  echo "Usage: ./run_client.sh <container-name>"
  exit
fi

# run client docker container with cmd args
docker run -it --rm --name "$1" \
 --network $PROJECT_NETWORK $CLIENT_IMAGE \
 java DataStoreClient
