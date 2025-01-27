package com.knifescout

import redis.clients.jedis.JedisPooled

private val redisHost = System.getenv("REDIS_HOST")
private val redisPort = 6379
val jedisPool = JedisPooled(redisHost, redisPort)