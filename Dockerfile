FROM ubuntu
RUN apt-get update -y && apt-get install nginx -y && service nginx start
COPY wedding-website/ /var/www/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

