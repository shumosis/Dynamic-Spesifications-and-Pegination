
import com.inflowsol.eduflow.dto.pagerequestparam.FilterCriteria;
import com.inflowsol.eduflow.util.EduFlowConstants;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class DynamicSpecifications {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$|^\\d{2}/\\d{2}/\\d{4}$|^\\d{2}-\\d{2}-\\d{4}$");

    public static <T> Specification<T> filterByForSchool(String column, Object filterBy) {
        return (root, query, Builder) -> Builder.equal(root.get("school").get(column), filterBy);
    }

    public static <T> Specification<T> and(List<Specification<T>> specs) {
        if (!specs.isEmpty()) {
            Specification<T> combinedSpec = Specification.where(specs.get(0));
            for (int i = 1; i < specs.size(); i++) {
                combinedSpec = combinedSpec.and(specs.get(i));
            }
            return combinedSpec;
        } else {
            return Specification.where(null);
        }
    }

    public static boolean isDate(Object dateString) {
        if (dateString instanceof CharSequence) {
            return DATE_PATTERN.matcher((CharSequence) dateString).matches();
        }
        return false;
    }

    public static LocalDate convertToDate(Object dateString) {
        String[] DATE_FORMATS = {"yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy"};
        for (String format : DATE_FORMATS) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate date = LocalDate.parse((CharSequence) dateString, formatter);
                return date;
            } catch (DateTimeParseException e) {
                continue;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date parsing failed");
    }

    public static <T> Specification<T> equal(String column, Object filterBy) {

        return (root, query, builder) -> {
            if (column.contains(".")) {
                String[] parts = column.split("\\.");
                Path<?> path = root;
                for (String part : parts) {
                    path = path.get(part);
                }
                return builder.equal(path, filterBy);
            } else {
                return builder.equal(root.get(column), filterBy);
            }
        };

    }

    public static <T> Specification<T> like(String column, Object filterBy) {
        return (root, query, builder) -> {
            if (column.contains(".")) {
                String[] parts = column.split("\\.");
                Path<String> path = root.get(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    path = path.get(parts[i]);
                }
                return builder.like(builder.lower(path.as(String.class)), "%" + convertToString(filterBy).toLowerCase() + "%");
            } else {
                return builder.like(builder.lower(root.get(column).as(String.class)), "%" + convertToString(filterBy).toLowerCase() + "%");
            }
        };
    }


    private static String convertToString(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).toString();
        } else {
            throw new IllegalArgumentException("Cannot convert value to String: " + value);
        }
    }


    public static <T> Specification<T> in(String column, Collection<?> filterBy) {
        return (root, query, builder) -> {
            Path<?> path = getPath(root, column);
            return path.in(filterBy);
        };
    }

    public static <T> Specification<T> isEmptyOrNull(String column) {
        return (root, query, builder) -> {
            Path<?> path = getPath(root, column);
            Path<Collection<?>> collectionPath = (Path<Collection<?>>) path;
            return builder.or(
                    builder.isEmpty(collectionPath)
            );
        };
    }

    public static <T> Specification<T> between(String column, Object lowerBound, Object upperBound) {
        if (isDate(lowerBound) && isDate(upperBound)) {
            LocalDate startDate = convertToDate(lowerBound);
            LocalDate endDate = convertToDate(upperBound);
            return (root, query, builder) -> {
                Path<Comparable> path = (Path<Comparable>) getPath(root, column);
                return builder.between(path, (Comparable) startDate, (Comparable) endDate);
            };
        } else {
            return (root, query, builder) -> {
                Path<Comparable> path = (Path<Comparable>) getPath(root, column);
                return builder.between(path, (Comparable) lowerBound, (Comparable) upperBound);
            };
        }
    }

    public static <T> Specification<T> greaterThan(String column, Object filterBy) {
        return (root, query, builder) -> {
            Path<Comparable> path = (Path<Comparable>) getPath(root, column);
            return builder.greaterThan(path, (Comparable) filterBy);
        };
    }


    public static <T> Specification<T> greaterThanOrEqualTo(String column, Object filterBy) {
        return (root, query, builder) -> {
            Path<Comparable> path = (Path<Comparable>) getPath(root, column);
            return builder.greaterThanOrEqualTo(path, (Comparable) filterBy);
        };
    }

    public static <T> Specification<T> lessThan(String column, Object filterBy) {
        return (root, query, builder) -> {
            Path<Comparable> path = (Path<Comparable>) getPath(root, column);
            return builder.lessThan(path, (Comparable) filterBy);
        };
    }

    public static <T> Specification<T> lessThanOrEqualTo(String column, Object filterBy) {
        return (root, query, builder) -> {
            Path<Comparable> path = (Path<Comparable>) getPath(root, column);
            return builder.lessThanOrEqualTo(path, (Comparable) filterBy);
        };
    }

    private static <T> Path<?> getPath(Root<T> root, String column) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            Path<?> path = root;
            for (String part : parts) {
                path = path.get(part);
            }
            return path;
        } else {
            return root.get(column);
        }
    }

    public Map<String, Object> getSpecList(Map<String, Object> entry) {
        Map<String, Object> column = new HashMap<>();

        for (Map.Entry<String, Object> entry1 : entry.entrySet()) {
            if (entry1.getValue() instanceof Map) {
                Map<String, Object> specListRecursiveNew = getSpecListRecursive(entry1.getKey() + ".", (Map<String, Object>) entry1.getValue());
                column.putAll(specListRecursiveNew);
            } else {
                column.put(entry1.getKey(), entry1.getValue());
            }
        }

        return column;
    }

    private Map<String, Object> getSpecListRecursive(String prefix, Map<String, Object> nestedValues) {
        Map<String, Object> columnValue = new HashMap<>();

        for (Map.Entry<String, Object> nestedEntry : nestedValues.entrySet()) {
            if (nestedEntry.getValue() instanceof Map) {
                Map<String, Object> specListRecursiveNew = getSpecListRecursive(prefix + nestedEntry.getKey() + ".", (Map<String, Object>) nestedEntry.getValue());
                columnValue.putAll(specListRecursiveNew);
            } else {
                columnValue.put(prefix + nestedEntry.getKey(), nestedEntry.getValue());
            }
        }

        return columnValue;
    }

    public <E> List<Specification<E>> convertFilters(Integer schoolId, List<FilterCriteria> filterCriteriaList) {
        List<Specification<E>> specList = new ArrayList<>();

        if (filterCriteriaList == null || filterCriteriaList.isEmpty()) {
            return specList;
        }

        for (FilterCriteria filterCriteria : filterCriteriaList) {
            Specification<E> spec = processFilterCriteria(filterCriteria);
            specList.add(spec);
        }

        return specList;
    }


    private <E> List<Specification<E>> initializeSpecifications(Integer schoolId) {
        List<Specification<E>> specList = new ArrayList<>();

        Specification<E> spec1 = equal("school.schoolId", schoolId);
        Specification<E> spec2 = equal("isDeleted", EduFlowConstants.NOT_DELETED);

        specList.add(spec1);
        specList.add(spec2);

        return specList;
    }

    private <E> Specification<E> processFilterCriteria(FilterCriteria filterCriteria) {
        Specification<E> spec = null;

        if (filterCriteria.getColumn() instanceof Map) {
            String columnPath = null;
            Object value = null;
            Map<String, Object> nestedColumn = (Map<String, Object>) filterCriteria.getColumn();
            Map<String, Object> specListNew = getSpecList(nestedColumn);

            for (Map.Entry<String, Object> nestedEntry : specListNew.entrySet()) {
                columnPath = nestedEntry.getKey();
                value = nestedEntry.getValue();
            }

            spec = createSpecification(filterCriteria, columnPath, value);
        }

        return spec;
    }

    private <E> Specification<E> createSpecification(FilterCriteria filterCriteria, String columnPath, Object value) {

        Object filterValue = getFilterValue(value);
        switch (filterCriteria.getOperation().toLowerCase()) {
            case EduFlowConstants.EQUALS_OPERATION:
                return equal(columnPath, filterValue);
            case EduFlowConstants.LIKE_OPERATION:
                return like(columnPath, filterValue);
            case EduFlowConstants.IN_OPERATION:
                return in(columnPath, filterCriteria.getInList());
            case EduFlowConstants.GREATER_THAN_OPERATION:
                return greaterThan(columnPath, filterValue);
            case EduFlowConstants.GREATER_THAN_OR_EQUAL_TO_OPERATION:
                return greaterThanOrEqualTo(columnPath, filterValue);
            case EduFlowConstants.LESS_THAN_OPERATION:
                return lessThan(columnPath, filterValue);
            case EduFlowConstants.LESS_THAN_OR_EQUAL_TO_OPERATION:
                return lessThanOrEqualTo(columnPath, filterValue);
            case EduFlowConstants.LIST_OPERATION:
                return isEmptyOrNull(columnPath);
            default:
                throw new IllegalArgumentException("Unsupported operation: " + filterCriteria.getOperation());
        }
    }

    private Object getFilterValue(Object value) {
        if (isDate(value)) {
            return convertToDate(value);
        } else if (value instanceof Boolean) {
            return Boolean.parseBoolean(value.toString());
        } else {
            return value;
        }
    }
}



