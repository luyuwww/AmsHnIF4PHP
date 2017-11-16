package junit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ams.service.i.OaDataRcvService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring/test*.xml" })
public class ShJdbcTest {
	
	@Test
	public void test02() {
//		String sql = "select count(*) from d_file1";
//		Integer count = jdbcTemplate.queryForInt(sql);
//		System.out.println(count);
		oaDataRcvService.dataReceive();
//		File[] listFile = new File("C:/test").listFiles();
//		for(File file : listFile){
//			File[] secondListFile = file.listFiles();
//			for(File secondFile : secondListFile){
//				System.out.println(secondFile.getName());
//			}
//		}
	}
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private OaDataRcvService oaDataRcvService;
}
