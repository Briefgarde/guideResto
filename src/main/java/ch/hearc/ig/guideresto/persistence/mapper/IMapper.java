package ch.hearc.ig.guideresto.persistence.mapper;
import java.util.HashSet;

public interface IMapper<T> {
    T findByID(int pk);
    HashSet<T> findAll();
    T insert(T t);
    T update(T t);
    void delete(T t);
}
