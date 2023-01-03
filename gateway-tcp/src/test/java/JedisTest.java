import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/12/17
 **/
@Ignore
public class JedisTest {

    @Test
    public void jedisTest(){
        Jedis jedis = new Jedis("127.0.0.1");
        jedis.set("aaa","bbb");
        System.out.println(jedis.get("aaa"));
    }
}
