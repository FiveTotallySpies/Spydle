FROM gradle:jdk17 as build
WORKDIR /app
COPY --chown=gradle:gradle . /app
RUN gradle :gameserver:build --no-daemon

# Start a new stage to create a smaller image
FROM amazoncorretto:17 as run
WORKDIR /app
COPY --from=build /app/gameserver/build/libs/*.jar /app/
COPY --from=build /app/gameserver/entry.sh /app/

ENTRYPOINT ["sh", "entry.sh"]