package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.dto.ContractDTO;
import wodss.timecastbackend.dto.EmployeeDTO;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.util.BadRequestException;
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Component
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ModelMapper mapper;

    public List<ContractDTO> findByQuery(LocalDate fromDate, LocalDate toDate) throws Exception {
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new BadRequestException();
        return modelsToDTOs(contractRepository.findByQuery(fromDate, toDate));
    }

    public ContractDTO findById(long id) throws Exception {
        ContractDTO contractDTO = null;
        Optional<Contract> oContract = contractRepository.findById(id);
        if(oContract.isPresent())
            contractDTO = mapper.contractToContractDTO(oContract.get());
        else
            throw new RessourceNotFoundException();
        return contractDTO;
    }

    public ContractDTO createContract(ContractDTO contractDTO) throws Exception {
        //checks
        Employee employee = checkIfEmployeeExists(contractDTO.getEmployeeId());
        checkPensumPercentage(contractDTO.getPensumPercentage());
        checkDates(contractDTO.getStartDate(), contractDTO.getEndDate(), contractDTO.getEmployeeId());
        //Creating contract
        Contract contract = new Contract(employee, contractDTO.getPensumPercentage(), contractDTO.getStartDate(), contractDTO.getEndDate());
        contract = contractRepository.save(contract);
        contractDTO = mapper.contractToContractDTO(contract);
        return contractDTO;
    }

    public void deleteContract(long id) throws Exception {
        Contract contract = checkIfContractExists(id);
        contractRepository.delete(contract);
    }

    public ContractDTO editContract(long id, ContractDTO contractDTO) throws Exception {
        //Checks
        Contract contract = checkIfContractExists(id);
        Employee employee = checkIfEmployeeExists(contractDTO.getEmployeeId());
        checkPensumPercentage(contractDTO.getPensumPercentage());
        checkDates(contractDTO.getStartDate(), contractDTO.getEndDate(), contractDTO.getEmployeeId());
        //Applying changes
        contract.setEmployee(employee);
        contract.setPensumPercentage(contractDTO.getPensumPercentage());
        contract.setStartDate(contractDTO.getStartDate());
        contract.setEndDate(contractDTO.getEndDate());
        contract = contractRepository.save(contract);

        return contractDTO;
    }

    public Contract checkIfContractExists(long id) throws Exception {
        Optional<Contract> oContract = contractRepository.findById(id);
        if(oContract.isPresent())
            return oContract.get();
        else
            throw new RessourceNotFoundException();
    }

    public Employee checkIfEmployeeExists(long id) throws Exception {
        Optional<Employee> oEmployee = employeeRepository.findById(id);
        if(oEmployee.isPresent())
            return oEmployee.get();
        else
            throw new RessourceNotFoundException();
    }

    public void checkPensumPercentage(int percentage) throws Exception{
        //Should this be handled inside of service? What if constraints are changed in model?
        if(percentage < 0 || percentage > 100){
            throw new PreconditionFailedException("The pensum percentage must lie within a range of 0 and 100.");
        }
    }

    public void checkDates(LocalDate startDate, LocalDate endDate, long employeeID) throws Exception{

        boolean startDateLiesAfterEndDate = startDate.isAfter(endDate);
        //boolean startDateIsInPast = startDate.isBefore(LocalDate.now());
        //Error cases:
        //Error case 1: The start date of the new Contract lies in between the start and end date of another contract of the same employee or it equals the start/end date of another contract.
        boolean startDateOverlapsWithOtherContract =
                contractRepository.findAll().stream()
                        .anyMatch(contract -> (
                                contract.getEmployee().getId() == employeeID)
                                && (((contract.getStartDate().isBefore(startDate) && contract.getEndDate().isAfter(startDate)))
                                || (contract.getStartDate().equals(startDate) || contract.getEndDate().equals(startDate)))
                        );
        //Error case 2: The end date lies in between the start and end date of another contract or the end date equals the start(end date of another contract).
        boolean endDateOverlapsWithOtherContract =
                contractRepository.findAll().stream()
                        .anyMatch(contract -> (
                                contract.getEmployee().getId() == employeeID)
                                &&((contract.getStartDate().isBefore(endDate) && contract.getEndDate().isAfter(endDate))
                                || (contract.getStartDate().equals(endDate) || contract.getEndDate().equals(endDate)))
                        );
        //Error case 3: There is another contract that is entirely contained within the contract.
        boolean contractContainsExistingContract =
                contractRepository.findAll().stream()
                        .anyMatch(contract -> (
                                contract.getEmployee().getId() == employeeID
                                && (contract.getStartDate().isAfter(startDate) && contract.getEndDate().isBefore(endDate))
                                )
                        );

        if(startDateLiesAfterEndDate){
            throw new PreconditionFailedException("The start date and end date must not overlap!");
        }
        if(startDateOverlapsWithOtherContract){
            throw new PreconditionFailedException("The start date overlaps with another contract!");
        }
        if(endDateOverlapsWithOtherContract){
            throw new PreconditionFailedException("The end date overlaps with another contract!");
        }
        if(contractContainsExistingContract){
            throw new PreconditionFailedException("There is already another contract in between the given time frame!");
        }
    }

    public List<ContractDTO> modelsToDTOs(List<Contract> allocations) {
        return allocations.stream().map(c -> mapper.contractToContractDTO(c)).collect(Collectors.toList());
    }
}
