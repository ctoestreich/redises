Adding the capability to use multiple connections for accessing Redis.  We may want to move this to the base plugin as an available options.

Adding additional redis connections:

resources.groovy
``` groovy
import redis.clients.jedis.JedisPool

// Place your Spring DSL code here
beans = {

    redisPool1(JedisPool, ref('redisPoolConfig'), 'localhost', 6379, 2000, '' ) { bean ->
        bean.destroyMethod = 'destroy'
    }

    redisPool2(JedisPool, ref('redisPoolConfig'), 'localhost', 6380, 2000, '' ) { bean ->
        bean.destroyMethod = 'destroy'
    }

    redisPool3(JedisPool, ref('redisPoolConfig'), 'localhost', 6381, 2000, '' ) { bean ->
        bean.destroyMethod = 'destroy'
    }
}
```

Injecting the new method onto the redis service:

BootStrap.groovy
``` groovy
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import grails.plugin.redis.RedisService

class BootStrap {

    def init = { servletContext ->

        RedisService.metaClass.withRedisPool = {JedisPool pool, Closure closure ->
            Jedis redis = pool.resource
            try {
                return closure(redis)
            } finally {
                pool.returnResource(redis)
            }
        }

    }
    def destroy = {
    }
}
```

See the RedisesSpec.groovy for the tests.  You will need to boot up some number of servers that match the host/port/pw in the resources.groovy.

``` groovy
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
```