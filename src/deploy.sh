PROJECT_NETWORK='mynetwork'
SERVER_IMAGE_1='myserver1'
SERVER_IMAGE_2='myserver2'
SERVER_IMAGE_3='myserver3'
SERVER_IMAGE_4='myserver4'
SERVER_IMAGE_5='myserver5'
SERVER_CONTAINER_1='myservercontainer1'
SERVER_CONTAINER_2='myservercontainer2'
SERVER_CONTAINER_3='myservercontainer3'
SERVER_CONTAINER_4='myservercontainer4'
SERVER_CONTAINER_5='myservercontainer5'
CLIENT_IMAGE='myclient'
CLIENT_CONTAINER='myclientcontainer'

# clean up existing resources, if any
echo "----------Cleaning up existing resources----------"
docker container stop $SERVER_CONTAINER_1 2> /dev/null && docker container rm $SERVER_CONTAINER_1 2> /dev/null
docker container stop $SERVER_CONTAINER_2 2> /dev/null && docker container rm $SERVER_CONTAINER_2 2> /dev/null
docker container stop $SERVER_CONTAINER_3 2> /dev/null && docker container rm $SERVER_CONTAINER_3 2> /dev/null
docker container stop $SERVER_CONTAINER_4 2> /dev/null && docker container rm $SERVER_CONTAINER_4 2> /dev/null
docker container stop $SERVER_CONTAINER_5 2> /dev/null && docker container rm $SERVER_CONTAINER_5 2> /dev/null
docker container stop $COORDINATOR_CONTAINER 2> /dev/null && docker container rm $COORDINATOR_CONTAINER 2> /dev/null
docker container stop $CLIENT_CONTAINER 2> /dev/null && docker container rm $CLIENT_CONTAINER 2> /dev/null
docker network rm $PROJECT_NETWORK 2> /dev/null

# only cleanup
if [ "$1" == "cleanup-only" ]
then
  exit
fi

# create a custom virtual network
echo "----------creating a virtual network----------"
docker network create $PROJECT_NETWORK

# build the images from Dockerfile
echo "----------Building images----------"
docker build -t $CLIENT_IMAGE --target client-build .
docker build -t $SERVER_IMAGE_1 --target server-build-1 .
docker build -t $SERVER_IMAGE_2 --target server-build-2 .
docker build -t $SERVER_IMAGE_3 --target server-build-3 .
docker build -t $SERVER_IMAGE_4 --target server-build-4 .
docker build -t $SERVER_IMAGE_5 --target server-build-5 .

# run the image and open the required ports
echo "----------Running registry and server app----------"
docker run -d -p 5000:5000 --name $SERVER_CONTAINER_1 --network $PROJECT_NETWORK $SERVER_IMAGE_1
docker run -d -p 5010:5010 --name $SERVER_CONTAINER_2 --network $PROJECT_NETWORK $SERVER_IMAGE_2
docker run -d -p 5020:5020 --name $SERVER_CONTAINER_3 --network $PROJECT_NETWORK $SERVER_IMAGE_3
docker run -d -p 5030:5030 --name $SERVER_CONTAINER_4 --network $PROJECT_NETWORK $SERVER_IMAGE_4
docker run -d -p 5040:5040 --name $SERVER_CONTAINER_5 --network $PROJECT_NETWORK $SERVER_IMAGE_5

echo "----------watching logs from registry and server----------"
docker logs $SERVER_CONTAINER_1 -f
docker logs $SERVER_CONTAINER_2 -f
docker logs $SERVER_CONTAINER_3 -f
docker logs $SERVER_CONTAINER_4 -f
docker logs $SERVER_CONTAINER_5 -f

