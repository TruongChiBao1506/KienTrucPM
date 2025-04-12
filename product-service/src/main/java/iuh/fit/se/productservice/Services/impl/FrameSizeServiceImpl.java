package iuh.fit.se.productservice.Services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.productservice.entities.FrameSize;
import iuh.fit.se.productservice.Repositories.FrameSizeRepository;
import iuh.fit.se.productservice.Services.FrameSizeService;

@Service
public class FrameSizeServiceImpl implements FrameSizeService{
	@Autowired
    FrameSizeRepository frameSizeRepository;
	@Override
	public FrameSize findById(Long id) {
		// TODO Auto-generated method stub
		return frameSizeRepository.findById(id).orElse(null) ;
	}

	@Override
	public List<FrameSize> findAll() {
		// TODO Auto-generated method stub
		return frameSizeRepository.findAll();
	}

	@Override
	public FrameSize save(FrameSize frameSize) {
		return frameSizeRepository.save(frameSize);
	}

	@Override
	public FrameSize update(Long id, FrameSize frameSize) {
		return null;
	}

	@Override
	public boolean delete(Long id) {
		return false;
	}
}
