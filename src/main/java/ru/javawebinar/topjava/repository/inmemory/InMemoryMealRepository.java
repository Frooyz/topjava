package ru.javawebinar.topjava.repository.inmemory;

import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private final Map<Integer, Meal> repository = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Meal save(Meal meal, int userId) {
            if (meal.isNew()) {
                meal.setId(counter.incrementAndGet());
                meal.setUserId(userId);
                repository.put(meal.getId(), meal);
                return meal;
            }
            // handle case: update, but not present in storage
            return repository.computeIfPresent(meal.getId(), (id, oldMeal) -> meal);
    }

    @Override
    public boolean delete(int id, int userId) {
        if (repository.get(id).getUserId().equals(userId)) {
            return repository.remove(id) != null;
        }
        return false;
    }

    @Override
    public Meal get(int id, int userId) {
        if (repository.get(id).getUserId().equals(userId)) {
            return repository.get(id);
        }
        return null;
    }

    @Override
    public Collection<Meal> getAll(int userId) {
        List<Meal> resultMeals = new ArrayList<>();
        for (var pair: repository.entrySet()){
            if (pair.getValue().getUserId().equals(userId)){
                resultMeals.add(pair.getValue());
            }
        }

        return resultMeals.stream()
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList());
//        return repository.entrySet().stream()
//                .filter(m -> m.getValue().getUserId().equals(userId))
//                .sorted((d1, d2) -> d1.getValue().getDate().compareTo(d2.getValue().getDate()))
//                .map();
    }
}

