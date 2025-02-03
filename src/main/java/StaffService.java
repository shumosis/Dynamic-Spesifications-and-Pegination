import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StaffService {

    public Page<StaffDto> findAllByCriteria(Integer schoolId, PageRequestParams pageRequestParams) {
        School school = schoolService.getSchool(schoolId);

        List<Specification<Staff>> specList = new ArrayList<>();

        List<Specification<Staff>> specs = dynamicSpecifications.convertFilters(schoolId, pageRequestParams.getFilterCriteria());
        specList.addAll(specs);

        Specification<Staff> andSpec = DynamicSpecifications.and(specList);

        Pageable pageable = PageableService.getPageable(pageRequestParams);
        Page<Staff> staff = staffRepository.findAll(andSpec, pageable);

        List<StaffDto> staffDtos = staff.getContent().stream()
                .map(ServiceConversionUtil::convertStaffToStaffDto)
                .collect(Collectors.toList());

        return new PageImpl<>(staffDtos, pageable, staff.getTotalElements());
    }

}
