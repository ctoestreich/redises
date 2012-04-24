package com.test

import grails.plugin.spock.IntegrationSpec
import redis.clients.jedis.Jedis

/**
 */
class RedisesSpec extends IntegrationSpec {

    def redisPool1
    def redisPool2
    def redisPool3
    def redisService

    def setup() {
        redisService.withRedisPool(redisPool1) { Jedis redis ->
            redis.flushDB()
        }
        redisService.withRedisPool(redisPool2) {Jedis redis ->
            redis.flushDB()
        }
        redisService.withRedisPool(redisPool3) {Jedis redis ->
            redis.flushDB()
        }
    }

    def "test multiple redis pools"() {
        given:
        def key = "key"
        def data = "data"

        when:
        redisService.withRedisPool(redisPool1) {Jedis redis ->
            redis.set(key, data)
        }

        then:
        redisService.withRedisPool(redisPool1) {Jedis redis ->
            redis.get(key) == data
        }
        redisService.withRedisPool(redisPool2) {Jedis redis ->
            !redis.get(key)
        }
        redisService.withRedisPool(redisPool3) {Jedis redis ->
            !redis.get(key)
        }

        when:
        redisService.withRedisPool(redisPool2) {Jedis redis ->
            redis.set(key, data)
        }

        then:
        redisService.withRedisPool(redisPool1) {Jedis redis ->
            redis.get(key) == data
        }
        redisService.withRedisPool(redisPool2) {Jedis redis ->
            redis.get(key) == data
        }
        redisService.withRedisPool(redisPool3) {Jedis redis ->
            !redis.get(key)
        }

        when:
        redisService.withRedisPool(redisPool3) {Jedis redis ->
            redis.set(key, data)
        }

        then:
        redisService.withRedisPool(redisPool1) {Jedis redis ->
            redis.get(key) == data
        }
        redisService.withRedisPool(redisPool2) {Jedis redis ->
            redis.get(key) == data
        }
        redisService.withRedisPool(redisPool3) {Jedis redis ->
            redis.get(key) == data
        }
    }
}
