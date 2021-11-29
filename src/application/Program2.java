package application;

import java.util.List;
import java.util.Scanner;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entities.Department;

public class Program2 {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		DepartmentDao departmentDao = DaoFactory.createDepartmentDao();
		
		System.out.println("=== TEST 1: department findById ====");
		Department department = departmentDao.findById(6);
		System.out.println(department);
		
		System.out.println("=== TEST 2: department insert ====");
		Department newDep = new Department(null, "Games");
		departmentDao.insert(newDep);
		System.out.println("Inserted! New ID = " + newDep.getId());
		
		System.out.println("=== TEST 3: department findAll ====");
		List<Department> list = departmentDao.findAll();
		list.forEach(System.out::println);
		
		System.out.println("=== TEST 4: department update ====");
		newDep = departmentDao.findById(9);
		newDep.setName("Furniture");
		departmentDao.update(newDep);
		
		System.out.println("=== TEST 5: department deleteById ====");
		System.out.println("Enter id for delete test: ");
		int n = sc.nextInt();
		departmentDao.deleteById(n);
		
		sc.close();

	}

}
