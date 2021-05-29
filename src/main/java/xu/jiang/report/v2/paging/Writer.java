package xu.jiang.report.v2.paging;

import java.io.File;
/**
 * 采用分页提取器的获取数据的{数据写入器]
 * @author jx
 *
 * @param <T> 数据提取器的分页形式
 * @param <E> 数据提取器的数据形式
 */
public interface Writer<T extends Paging> {
	File writeByPaging(T paging) throws  Exception;
}
