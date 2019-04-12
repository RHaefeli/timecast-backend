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
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public List<AllocationDTO> findAll() {
        List<AllocationDTO> allocations = allocationRepository.findAll().stream().map(a -> mapper.allocationToAllocationDTO(a)).collect(Collectors.toList());
        return allocations;
    }

    public AllocationDTO findById(long id) throws Exception {
        Allocation allocation = checkIfAllocationExists(id);
        AllocationDTO allocationDTO = mapper.allocationToAllocationDTO(allocation);
        return allocationDTO;
    }

    public AllocationDTO createAllocation(AllocationDTO allocationDTO) throws Exception {
        Contract contract = checkIfContractExists(allocationDTO.getContractId());
        Project project = checkIfProjectExists(allocationDTO.getProjectId());
        checkIfAllocationIsValid(allocationDTO, project, contract);
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
        checkIfAllocationIsValid(allocationDTO, project, contract);
        allocation.setContract(contract);
        allocation.setProject(project);
        allocation.setPensumPercentage(allocationDTO.getPensumPercentage());
        allocation.setStartDate(allocationDTO.getStartDate());
        allocation.setEndDate(allocationDTO.getEndDate());
        allocationRepository.save(allocation);
        allocationDTO.setId(id);
        return allocationDTO;
    }

    private Allocation checkIfAllocationExists(long id) throws Exception {
        Optional<Allocation> oAllocation = allocationRepository.findById(id);
        if(oAllocation.isPresent())
            return oAllocation.get();
        else
            throw new RessourceNotFoundException();
    }

    private Contract checkIfContractExists(long id) throws Exception {
        Optional<Contract> oContract = contractRepository.findById(id);
        if(oContract.isPresent())
            return oContract.get();
        else
            throw new RessourceNotFoundException();
    }

    private Project checkIfProjectExists(long id) throws Exception {
        Optional<Project> oProject = projectRepository.findById(id);
        if(oProject.isPresent())
            return oProject.get();
        else
            throw new RessourceNotFoundException();
    }

    private void checkIfAllocationIsValid(AllocationDTO allocationDTO, Project project, Contract contract) throws Exception{
        //Do all checks in one method.
        checkIfAllocationPensumPercentageIsNegative(allocationDTO.getPensumPercentage());
        checkDates(allocationDTO.getStartDate(), allocationDTO.getEndDate());
        checkIfAllocationExceedsFTE(project, allocationDTO.getPensumPercentage());
    }

    private void checkIfAllocationPensumPercentageIsNegative(int allocationPensumPercentage) throws Exception{
        if(allocationPensumPercentage < 0) throw new PreconditionFailedException("Pensum must not be negative");
    }

    private void checkDates(LocalDate startDate, LocalDate endDate) throws Exception{
        if(startDate.isAfter(endDate)){
            throw new PreconditionFailedException("Start date lies after end date.");
        }
    }

    private void checkIfAllocationExceedsFTE(Project project, int allocationPensumPercentage) throws Exception{
        int FTEs = allocationRepository.findAll().stream()
                .filter(allocation -> allocation.getProject().getId() == project.getId())
                .mapToInt(allocation -> allocation.getPensumPercentage())
                .sum();
        if(FTEs + allocationPensumPercentage > project.getFtePercentage()){
            throw new PreconditionFailedException("The allocation's pensum percentage exceeds the planned FTEs of the project");
        }
    }

    private void checkIfAllocationFitsInContract(LocalDate startDate, LocalDate endDate, Contract contract) throws Exception{
        boolean startDateLiesOutsideOfContract = startDate.isBefore(contract.getStartDate());
        boolean endDateLiesOutsideOfContract = endDate.isAfter(contract.getEndDate());
        if(startDateLiesOutsideOfContract) throw new PreconditionFailedException("The start date of this allocation lies outside of the employee's contract.");
        if(endDateLiesOutsideOfContract) throw new PreconditionFailedException("The end date of this allocation lies outside of the employee's contract.");
    }

    private void checkIfAllocationExeedsEmployeeLimit(Contract contract, LocalDate startDate, LocalDate endDate, int allocationPensumPercentage) throws Exception {
        List<Allocation> overlappingAllocations = allocationRepository.findAll().stream().filter(a ->
                a.getContract().getId() == contract.getId()
                && (
                        dateIsWithinRange(a, startDate)
                        || dateIsWithinRange(a, endDate)
                        || allocationContainedWithinDates(a, startDate, endDate)
                )
                ).collect(Collectors.toList());
        //TODO: Modification for recursion algorithm. The recursion needs to check overlappers for every allocation.
        //Each allocation might have new overlappers. Maybe check in with someone else.
        for(Allocation a : overlappingAllocations){
            if(recursion(overlappingAllocations,allocationPensumPercentage ).sum > contract.getPensumPercentage()){
                throw new PreconditionFailedException("The new allocation would lead to an exceeded pensum percentage for this contract.");
            }
        }
    }

    /**
     * Returns a RecursionItem which contains the sum of the pensum percentage during the duration of the new allocation.
     * @param startList A list of allocations, which contains all allocations that the new allocation overlaps with.
     * @param startPercentage the pensum percentage that will be added to the sum.
     * @return A recursion item containing the sum of pensum percentages.
     */
    private RecursionItem recursion(List<Allocation> startList, int startPercentage){
        RecursionItem result = new RecursionItem();
        int sum = 0;

        List<Allocation> found = new ArrayList<>();

        for(Allocation allocation : startList){
            if(!found.contains(allocation)){
                List<Allocation> overlapsWithAllocation = startList.stream().filter(a ->
                        a.getId() != allocation.getId()
                        &&(dateIsWithinRange(a, allocation.getStartDate())
                        || dateIsWithinRange(a, allocation.getEndDate())
                        || allocationContainedWithinDates(a, allocation.getStartDate(), allocation.getEndDate())
                        )
                ).collect(Collectors.toList());
                RecursionItem rec = new RecursionItem(recursion(overlapsWithAllocation, allocation.getPensumPercentage()));
                sum += rec.sum;
                found = rec.foundAllocations;
                found.add(allocation);
            }
        }
        result.sum = sum + startPercentage;
        result.foundAllocations = found;
        return result;

    }

    private class RecursionItem{
        protected int sum;
        protected List<Allocation> foundAllocations = new ArrayList<>();
        protected RecursionItem(){}
        protected RecursionItem(RecursionItem r){
            sum = r.sum;
            foundAllocations = r.foundAllocations;
        }
    }

    /**
     * Checks if the date lies within the date range of the passed allocation or equals either the start or end date.
     * @param a allocation which contains the date range
     * @param localDate the date that needs to be checked
     * @return true if the date lies within the range of the allocation
     */
    private boolean dateIsWithinRange(Allocation a, LocalDate localDate){
        return (a.getStartDate().isBefore(localDate) && a.getEndDate().isAfter(localDate))
            || (a.getStartDate().equals(localDate) || a.getEndDate().equals(localDate));
    }

    /**
     * Checks if the allocation lies in between the start and end date.
     * @param a the allocation
     * @param startDate start date of the new allocation
     * @param endDate end date of the new allocation
     * @return true if the allocation 'a' lies in between the start and end date.
     */
    private boolean allocationContainedWithinDates(Allocation a, LocalDate startDate, LocalDate endDate){
        return (a.getStartDate().isAfter(startDate) && a.getEndDate().isBefore(endDate));
    }

}
