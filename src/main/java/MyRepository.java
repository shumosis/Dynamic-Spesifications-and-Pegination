@NoRepositoryBean
public interface MyRepository<T> extends JpaSpecificationExecutor<T> {
    Page<T> findAll(Specification<T> specification, Pageable pageable);
}
