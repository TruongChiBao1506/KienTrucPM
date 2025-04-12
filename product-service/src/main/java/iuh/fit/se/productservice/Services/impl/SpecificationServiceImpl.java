package iuh.fit.se.productservice.Services.impl;

import java.util.List;

import iuh.fit.se.productservice.Repositories.SpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.productservice.entities.Specifications;
import iuh.fit.se.productservice.Services.SpecificationService;

@Service
public class SpecificationServiceImpl implements SpecificationService{
	@Autowired
	private SpecificationRepository specificationRepository;

	@Override
	public Specifications findById(Long id) {
		
		return specificationRepository.findById(id).orElse(null) ;
	}

	@Override
	public List<Specifications> findAll() {
		// TODO Auto-generated method stub
		return specificationRepository.findAll();
	}

	@Override
	public Specifications save(Specifications specifications) {
		return specificationRepository.save(specifications);
	}

	@Override
	public Specifications update(Long id, Specifications specifications) {
		return null;
	}

	@Override
	public boolean delete(Long id) {
		return false;
	}

}
