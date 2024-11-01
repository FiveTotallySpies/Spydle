FROM gradle:jdk17 as build
WORKDIR /app
COPY --chown=gradle:gradle . /app
RUN gradle :gameserver:build --no-daemon

# Start a new stage to create a smaller image
FROM amazoncorretto:17 as run
WORKDIR /app
COPY --from=build /app/gameserver/build/libs/*.jar /app/
COPY --from=build /app/gameserver/entry.sh /app/
# Spring app
EXPOSE 8080
# Netty
EXPOSE 7654

ENTRYPOINT ["sh", "entry.sh"]