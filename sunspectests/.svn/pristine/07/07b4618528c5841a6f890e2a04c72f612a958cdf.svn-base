#!/bin/bash
echo "stopping tomcat"
sudo service tomcat8 stop
echo "stopping tomcat ......."
sleep 2
echo "deleting  tomcat log"
sudo rm /var/log/tomcat8/catalina.out
sleep 2
echo "deleting old entry"
sudo sed -i.bak '/'$1'/d' $3
echo "inserting new entry for $1"
sudo sed -i.bak '/arrayInd/a \"'$1'\" : '$2',' $3
echo "restart  tomcat"
sudo service tomcat8 restart
sleep 2
echo "Waiting for tomcat to restart..."
while true ; do
  echo "Working..."
  result=$(grep -i 'Catalina.start Server startup in' /var/log/tomcat8/catalina.out) # -n shows line number
  if [ "$result" ] ; then
    echo "COMPLETE!"
    echo "Result found is $result"
    break
  fi
  sleep 2
done
