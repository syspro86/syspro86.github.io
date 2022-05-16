docker run -it --rm --tty \
  -v /etc/localtime:/etc/localtime:ro \
  -v "$PWD":/usr/src/app \
  -p 4000:4000 \
  starefossen/github-pages
