#-------------------------------------------------------------------------------
# Install this script on /home/powin of the turtle
#-------------------------------------------------------------------------------
echo "Restarting Tomcat..."
sudo service tomcat8 stop 
sleep 2
sudo rm -rf /var/log/tomcat8/catalina.out /var/log/tomcat8/catalina_old1.out
sudo service tomcat8 restart 
while true ; do 
echo "Waiting for tomcat to restart..."
result=$(grep -i "Catalina.start Server startup in" /var/log/tomcat8/catalina.out) # -n shows line number 
if [ "$result" ] ; then 
echo "COMPLETE!" 
echo "Result found is $result"
break 
fi 
sleep 2
done
