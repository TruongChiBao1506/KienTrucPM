package iuh.fit.se.productservice.Services;

import java.util.List;

import iuh.fit.se.productservice.entities.FrameSize;

public interface FrameSizeService {
    List<FrameSize> findAll();
    FrameSize findById(Long id);
    FrameSize save(FrameSize frameSize);
    FrameSize update(Long id, FrameSize frameSize);
    boolean delete(Long id);
}