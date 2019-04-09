package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.Allocation;
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.domain.Project;
import wodss.timecastbackend.dto.AllocationDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.ProjectRepository;
import wodss.timecastbackend.util.*;

import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Component
public class AllocationService {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModelMapper mapper;

    public List<AllocationDTO> findbyQuery(Long employeeId, Long projectId, LocalDate fromDate, LocalDate toDate)
            throws Exception {
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new BadRequestException();
        return modelsToDTOs(allocationRepository.findByQuery(employeeId, projectId, fromDate, toDate));
    }

    public AllocationDTO findById(long id) throws Exception {
        Allocation allocation = checkIfAllocationExists(id);
        AllocationDTO allocationDTO = mapper.allocationToAllocationDTO(allocation);
        return allocationDTO;
    }

    public AllocationDTO createAllocation(AllocationDTO allocationDTO) throws Exception {
        Contract contract = checkIfContractExists(allocationDTO.getContractId());
        Project project = checkIfProjectExists(allocationDTO.getProjectId());
        Allocation allocation = new Allocation(project, contract, allocationDTO.getPensumPercentage(), allocationDTO.getStartDate(), allocationDTO.getEndDate());
        allocation = allocationRepository.save(allocation);
        allocationDTO = mapper.allocationToAllocationDTO(allocation);
        return allocationDTO;
    }

    public void deleteAllocation(long id) throws Exception {
        Allocation allocation = checkIfAllocationExists(id);
        allocationRepository.delete(allocation);
    }

    public AllocationDTO editAllocation(long id, AllocationDTO allocationDTO) throws Exception {
        Allocation allocation = checkIfAllocationExists(id);
        Contract contract = checkIfContractExists(id);
        Project project = checkIfProjectExists(id);
        allocation.setContract(contract);
        allocation.setProject(project);
        allocation.setPensumPercentage(allocationDTO.getPensumPercentage());
        allocation.setStartDate(allocationDTO.getStartDate());
        allocation.setEndDate(allocationDTO.getEndDate());
        allocationRepository.save(allocation);
        allocationDTO.setId(id);
        return allocationDTO;
    }

    public Allocation checkIfAllocationExists(long id) throws Exception {
        Optional<Allocation> oAllocation = allocationRepository.findById(id);
        if(oAllocation.isPresent())
            return oAllocation.get();
        else
            throw new RessourceNotFoundException();
    }

    public Contract checkIfContractExists(long id) throws Exception {
        Optional<Contract> oContract = contractRepository.findById(id);
        if(oContract.isPresent())
            return oContract.get();
        else
            throw new PreconditionFailedException();
    }

    public Project checkIfProjectExists(long id) throws Exception {
        Optional<Project> oProject = projectRepository.findById(id);
        if(oProject.isPresent())
            return oProject.get();
        else
            throw new RessourceNotFoundException();
    }

    public void checkDates(LocalDate startDate, LocalDate endDate) throws Exception{
        if(startDate.isAfter(endDate)){
            throw new PreconditionFailedException();
        }
    }
    public void pensumCheck(AllocationDTO allocationDTO){
        
    }

     public List<AllocationDTO> modelsToDTOs(List<Allocation> allocations) {
        return allocations.stream().map(a -> mapper.allocationToAllocationDTO(a)).collect(Collectors.toList());
    }
}
