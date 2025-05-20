package iuh.fit.se.productservice.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "glasses")
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Glass {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String brand;
	private Double price;
    private String colorName;
    private String colorCode;
    private String imageFrontUrl;
    private String imageSideUrl;
	private boolean gender;
	private int stock;
	private String description;
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "specifications_id")
//	@JsonIgnore
	private Specifications specifications;
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "frame_size_id")
//	@JsonIgnore
	private FrameSize frameSize;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id")
//	@JsonIgnore
	private Category category;



	

	


	
}
