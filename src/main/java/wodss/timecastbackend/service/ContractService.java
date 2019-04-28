package wodss.timecastbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import wodss.timecastbackend.domain.Contract;
import wodss.timecastbackend.domain.Employee;
import wodss.timecastbackend.domain.Role;
import wodss.timecastbackend.dto.ContractDTO;
import wodss.timecastbackend.persistence.AllocationRepository;
import wodss.timecastbackend.persistence.ContractRepository;
import wodss.timecastbackend.persistence.EmployeeRepository;
import wodss.timecastbackend.security.EmployeeSession;
import wodss.timecastbackend.util.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Component
public class ContractService {

    private ContractRepository contractRepository;
    private EmployeeRepository employeeRepository;
    private AllocationRepository allocationRepository;
    private ModelMapper mapper;
    private EmployeeSession employeeSession;

    @Autowired
    public ContractService(ContractRepository contractRepository, EmployeeRepository employeeRepository,
                           AllocationRepository allocationRepository, ModelMapper mapper, EmployeeSession session) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.allocationRepository = allocationRepository;
        this.mapper = mapper;
        this.employeeSession = session;
    }

    /**
     * Find contracts with optional query parameters. The qeury options do stack (AND association).
     * ADMINISTRATOR, PROJECTMANAGER: Access all contracts.
     * DEVELOPER: Access only own contracts.
     * @param fromDate Get all contracts starting from date
     *                 (Contracts with start date before and end date after are included)
     * @param toDate Get all contracts until date
     *               (Contracts with start date before and end date after are included)
     * @return List of contract DTOs
     * @throws PreconditionFailedException Query parameter fromDate is after toDate
     */
    public List<ContractDTO> findByQuery(LocalDate fromDate, LocalDate toDate) throws PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();
        Long employeeId = null;
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new PreconditionFailedException();

        //DEVELOPER (Only ADMINISTRATOR, PROJECTAMANAGER can see all projects)
        if(currentEmployee.getRole() != Role.ADMINISTRATOR && currentEmployee.getRole() != Role.PROJECTMANAGER)
            employeeId = currentEmployee.getId();

        return modelsToDTOs(contractRepository.findByQuery(employeeId, fromDate, toDate));
    }

    /**
     * Find contract by id.
     * ADMINISTRATOR, PROJECTMANAGER: Access to all contracts. Get informed if contract does not exists.
     * DEVELOPER: Access to only own contracts. Gets 403 if accesses to any other contract (even if it does not
     *            exist).
     * @param id Identifier of the requested allocation
     * @return Allocation DTO of requested allocation
     * @throws ForbiddenException Developer tries to access foreign contract
     * @throws ResourceNotFoundException Administrator or projectmanager access to non existing contract
     */
    public ContractDTO findById(long id) throws ResourceNotFoundException, ForbiddenException {
        Employee currentEmployee = employeeSession.getEmployee();
        Contract contract = null;

        //ADMINISTRATOR, PROJECTMANAGER
        if(currentEmployee.getRole() == Role.ADMINISTRATOR || currentEmployee.getRole() == Role.PROJECTMANAGER)
            contract = checkIfContractExists(id);

        //DEVELOPER
        else {
            try {
                contract = checkIfContractExists(id);
                if(contract.getEmployee().getId() != currentEmployee.getId())
                    throw new Exception();
            } catch (Exception e) {
                throw new ForbiddenException(
                        "Missing permission to get the contract (DEVELOPER: Somebody's else's contract");
            }
        }

        return mapper.contractToContractDTO(contract);
    }

    /**
     * Create a new contract
     * ADMIN: Able to create contracts.
     * PROJECTMANAGER, DEVELOPER: Not able to create contracts at all.
     * @param contractDTO Received and validated contract DTO object
     * @return Contract DTO of newly created contract
     * @throws ForbiddenException Developer or projectmanager tried to create new contract
     * @throws ResourceNotFoundException Employee referred to does not exist
     * @throws PreconditionFailedException Contract date range overlaps with another contract date range on the same
     *                                     employee
     */
    public ContractDTO createContract(ContractDTO contractDTO)
            throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee.getRole() == Role.ADMINISTRATOR) {
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

        //PROJECTMANAGER, DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to create a contract (PROJECTMANAGER, DEVELOPER)");
        }
    }

    /**
     * Delete a contract.
     * ADMIN: Able to delete contracts.
     * PROJECTMANAGER, DEVELOPER: Not able to delete contracts at all.
     * @param id Identifier of the requested contract to delete
     * @throws ForbiddenException Developer or projectmanager tried to delete a contract
     * @throws ResourceNotFoundException Contract with id does not exist
     * @throws PreconditionFailedException The contract is in use by an allocation
     */
    public void deleteContract(long id)
            throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee.getRole() == Role.ADMINISTRATOR){
            Contract contract = checkIfContractExists(id);
            checkIfContractIsInUse(id);
            contractRepository.delete(contract);
        }

        //PROJECTMANAGER, DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to delete the contract (PROJECTMANAGER, DEVELOPER)");
        }
    }

    /**
     * Update a contract.
     * ADMINISTRATOR: Able to update all contracts.
     * PROJECTMANAGER, DEVELOPER: Not able to update contracts at all.
     * @param id Identifier of the requested contract
     * @param contractDTO Validated contract DTO with updated fields
     * @return Contract DTO of the updated contract
     * @throws ForbiddenException Developer or Projectmanager tried to update a contract
     * @throws ResourceNotFoundException Contract with id does not exist, employee does not exist
     * @throws PreconditionFailedException Contract date range overlaps with another contract date range on the same
     *                                     employee
     */
    public ContractDTO updateContract(long id, ContractDTO contractDTO)
            throws ForbiddenException, ResourceNotFoundException, PreconditionFailedException {
        Employee currentEmployee = employeeSession.getEmployee();

        //ADMINISTRATOR
        if(currentEmployee.getRole() == Role.ADMINISTRATOR) {
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
            contractDTO.setId(id);
            return contractDTO;
        }

        //PROJECTMANAGER, DEVELOPER
        else {
            throw new ForbiddenException(
                    "Missing permission to update the contract (PROJECTMANAGER, DEVELOPER)");
        }
    }

    //Helper Methods

    public Contract checkIfContractExists(long id) throws ResourceNotFoundException {
        Optional<Contract> oContract = contractRepository.findById(id);
        if(oContract.isPresent())
            return oContract.get();
        else
            throw new ResourceNotFoundException();
    }

    public Employee checkIfEmployeeExists(long id) throws ResourceNotFoundException {
        Optional<Employee> oEmployee = employeeRepository.findById(id);
        if(oEmployee.isPresent())
            return oEmployee.get();
        else
            throw new ResourceNotFoundException();
    }

    public void checkPensumPercentage(int percentage) throws PreconditionFailedException{
        //Should this be handled inside of service? What if constraints are changed in model?
        if(percentage < 0 || percentage > 100){
            throw new PreconditionFailedException("The pensum percentage must lie within a range of 0 and 100.");
        }
        return percentage;
    }

    }

    public void checkDates(LocalDate startDate, LocalDate endDate, long employeeID) throws PreconditionFailedException{

        boolean startDateLiesAfterEndDate = startDate.isAfter(endDate);
        //boolean startDateIsInPast = startDate.isBefore(LocalDate.now());
        //Error cases:
        //Error case 1: The start date of the new Contract lies in between the start and end date of another contract of the same employee or it equals the start/end date of another contract.
        boolean startDateOverlapsWithOtherContract =
                contractRepository.findAll().stream()
                        .anyMatch(contract -> (
                                contract.getId() != contractID
                                && contract.getEmployee().getId() == employeeID)
                                && (((contract.getStartDate().isBefore(startDate) && contract.getEndDate().isAfter(startDate)))
                                || (contract.getStartDate().equals(startDate) || contract.getEndDate().equals(startDate)))
                        );
        //Error case 2: The end date lies in between the start and end date of another contract or the end date equals the start(end date of another contract).
        boolean endDateOverlapsWithOtherContract =
                contractRepository.findAll().stream()
                        .anyMatch(contract -> (
                                contract.getId() != contractID
                                && contract.getEmployee().getId() == employeeID)
                                &&((contract.getStartDate().isBefore(endDate) && contract.getEndDate().isAfter(endDate))
                                || (contract.getStartDate().equals(endDate) || contract.getEndDate().equals(endDate)))
                        );
        //Error case 3: There is another contract that is entirely contained within the contract.
        boolean contractContainsExistingContract =
                contractRepository.findAll().stream()
                        .anyMatch(contract -> (
                                contract.getId() != contractID
                                &&contract.getEmployee().getId() == employeeID
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

    private void checkIfContractIsInUse(long contractId) throws PreconditionFailedException {
        if(allocationRepository.existsByContractId(contractId))
            throw new PreconditionFailedException("Precondition for the contract failed");
    }

    public List<ContractDTO> modelsToDTOs(List<Contract> allocations) {
        return allocations.stream().map(c -> mapper.contractToContractDTO(c)).collect(Collectors.toList());
    }
}
