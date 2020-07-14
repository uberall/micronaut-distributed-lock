package com.uberall;

import com.uberall.models.Lock;

import javax.inject.Inject;
import java.util.Optional;

public class MicronautDataJdbcLockService implements LockService {

    private final DistributedLockRepository distributedLockRepository;

    @Inject
    public MicronautDataJdbcLockService(DistributedLockRepository distributedLockRepository) {
        this.distributedLockRepository = distributedLockRepository;
    }

    @Override
    public Optional<Lock> get(String name) {
        Optional<DistributedLock> maybeDistributedLock = distributedLockRepository.findByName(name);
        if (!maybeDistributedLock.isPresent()) {
            return Optional.empty();
        }

        DistributedLock dl = maybeDistributedLock.get();
        return Optional.of(new Lock(dl.name, dl.until));
    }

    @Override
    public void create(Lock lock) {
        distributedLockRepository.save(new DistributedLock(lock.getName(), lock.getUntil()));
    }

    @Override
    public void delete(Lock lock) {
        distributedLockRepository.findByName(lock.getName())
                .ifPresent(distributedLockRepository::delete);
    }

    @Override
    public void clear() {
        distributedLockRepository.deleteAll();
    }
}
