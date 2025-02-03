
import com.inflowsol.eduflow.dto.pagerequestparam.PageRequestParams;
import com.inflowsol.eduflow.util.EduFlowConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PageableService {

    public static Pageable getPageable(PageRequestParams pageRequestParams) {
        if (pageRequestParams == null) {
            pageRequestParams = new PageRequestParams();
        }
        pageRequestParams.setDefaultValuesIfMissing();

        return buildPageable(pageRequestParams);
    }

    private static Pageable buildPageable(PageRequestParams pageRequestParams) {
        Sort sort = buildSortFromCriteria(pageRequestParams);
        Integer pageNumber = pageRequestParams.getPage();
        Integer size = pageRequestParams.getSize();

        return sort != null ? PageRequest.of(pageNumber, size, sort) : PageRequest.of(pageNumber, size);
    }


    private static Sort buildSortFromCriteria(PageRequestParams pageRequestParams) {
        List<Object> sortCriteria = pageRequestParams.getSortCriteria();
        Sort sort = null;

        if (sortCriteria != null && !sortCriteria.isEmpty()) {
            for (Object obj : sortCriteria) {
                if (obj instanceof Map) {
                    Map<String, Object> criteriaMap = (Map<String, Object>) obj;
                    for (Map.Entry<String, Object> entry : criteriaMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        sort = processSortEntry(key, value, sort);
                    }
                } else {
                    throw new IllegalArgumentException("Invalid sort criteria format: " + obj.getClass().getSimpleName());
                }
            }
        }

        return sort;
    }

    private static Sort processSortEntry(String key, Object value, Sort sort) {
        if (value instanceof Map) {
            Map<String, String> nestedMap = (Map<String, String>) value;
            for (Map.Entry<String, String> nestedEntry : nestedMap.entrySet()) {
                String nestedKey = nestedEntry.getKey();
                String direction = nestedEntry.getValue();
                Sort.Direction sortDirection = EduFlowConstants.DESCENDING.equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
                sort = appendSortCriteria(sort, key + "." + nestedKey, sortDirection);
            }
        } else if (value instanceof String) {
            String direction = (String) value;
            Sort.Direction sortDirection = EduFlowConstants.DESCENDING.equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = appendSortCriteria(sort, key, sortDirection);
        } else {
            throw new IllegalArgumentException("Invalid sort criteria format: " + value.getClass().getSimpleName());
        }

        return sort;
    }

    private static Sort appendSortCriteria(Sort sort, String key, Sort.Direction direction) {
        if (sort == null) {
            return Sort.by(direction, key);
        } else {
            return sort.and(Sort.by(direction, key));
        }
    }

}

