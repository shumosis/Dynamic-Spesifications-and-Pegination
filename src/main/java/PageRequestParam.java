import java.time.LocalDate;
import java.util.List;
@Data
public class PageRequestParam {

    @Data
    public class PageRequestParams {

        Integer page;
        Integer size;
        LocalDate startDate ;
        LocalDate endDate;
        List<FilterCriteria> filterCriteria;
        List<Object> sortCriteria;


        public boolean isPageProvided() {
            return page != null;
        }

        public boolean isSizeProvided() {
            return size != null;
        }

        public void setDefaultValuesIfMissing() {
            if (!isPageProvided()) {
                setPage(0);
            }

            if (!isSizeProvided()) {
                setSize(10);
            }

        }
    }
}
