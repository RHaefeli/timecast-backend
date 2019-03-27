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
import wodss.timecastbackend.util.ModelMapper;
import wodss.timecastbackend.util.PreconditionFailedException;
import wodss.timecastbackend.util.RessourceNotFoundException;

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

    public List<ContractDTO> findAll() {
        List<ContractDTO> contractDTOs = contractRepository.findAll().stream().map(c -> mapper.contractToContractDTO(c)).collect(Collectors.toList());
        return contractDTOs;
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
        Employee employee = checkIfEmployeeExists(contractDTO.getEmployeeId());
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
        Contract contract = checkIfContractExists(id);
        Employee employee = checkIfEmployeeExists(id);
        contract.setEmployee(employee);
        contract.setPensumPercentage(contractDTO.getPensumPercentage());
        contract.setStartDate(contractDTO.getStartDate());
        contract.setEndDate(contractDTO.getEndDate());
        contract = contractRepository.save(contract);
        contractDTO.setId(id);
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
            throw new PreconditionFailedException();
    }
}
