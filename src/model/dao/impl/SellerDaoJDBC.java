package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;// Dependencia de conexão com o JDBC

	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}
	
	// Insere dados
	@Override
	public void insert(Seller obj) {
		PreparedStatement st = null;
		
		try {
			st = conn.prepareStatement(
					"INSERT INTO seller"
							+ "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
							+ "Values"
							+ "(?, ?, ?, ?, ?)", 
							Statement.RETURN_GENERATED_KEYS
					);
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			
			int rowsAffected = st.executeUpdate();
			
			// Verifica se os dados foram inseridos
			if (rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				
				// Enquanto houver elementos
				if (rs.next()) {
					
					// Atribui o Id ao objeto Seller
					int id = rs.getInt(1);
					obj.setId(id);	
				}
				DB.closeResultSet(rs);
			}
			else {
				throw new DbException("Unexpected error! No rows affected!");
			}
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}
				

	}

	// Atualiza Dados
	@Override
	public void update(Seller obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"UPDATE seller "
					+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
					+ "WHERE Id = ?");
			
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			st.setInt(6, obj.getId());
			
			st.executeUpdate();
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}
	}

	
	// Deleta com base no Id
	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("DELETE FROM seller WHERE Id = ?");
			st.setInt(1, id);
			
			int rowsAffected = st.executeUpdate();
			String result = rowsAffected > 0 ? "Completed delete!" : "No rows were deleted";
			System.out.println(result);
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}

	}

	// Faz uma busca a partir do ID
	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			// Query a ser executada
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "WHERE seller.Id = ?");
			// Id que será buscado
			st.setInt(1, id);
			// Executa a query
			rs = st.executeQuery();
			// Caso a consulta nao retorne nenhum registro, retorna falso
			// Caso contrário, um novo objeto Seller é instanciado recebendo um Department
			// como parâmetro
;
			if (rs.next()) {

				// Objeto Department Instanciado
				Department dep = instantiateDepartment(rs);

				// Instancia um Seller, recebendo um resultSet como argumento, e um objeto
				// Department
				Seller obj = instantiateSeller(rs, dep);
				return obj;
			}
			return null;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	// Instancia um seller
	private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
		Seller obj = new Seller();

		// Aplica os atributos no objeto Seller
		obj.setId(rs.getInt("Id"));
		obj.setName(rs.getString("Name"));
		obj.setEmail(rs.getString("Email"));
		obj.setBaseSalary(rs.getDouble("BaseSalary"));
		obj.setBirthDate(rs.getDate("BirthDate"));
		obj.setDepartment(dep);// Recebe o objeto Department montado no início do bloco
		return obj;
	}
	
	
	// Instancia um department
	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department dep = new Department();
		new Department();
		dep.setId(rs.getInt("DepartmentId"));// Recebe o id do departamento
		dep.setName(rs.getString("DepName"));// Recebe o nome do departamento

		return dep;
	}

	// Retorna todos os elementos
	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "ORDER BY Name");

			rs = st.executeQuery();

			List<Seller> list = new ArrayList<>();

			// Map para armazenar todos os departamentos instanciados
			Map<Integer, Department> map = new HashMap<>();

			// Enquanto houver elementos

			while (rs.next()) {

				// Verifica se o objeto Department já existe pelo Id
				// Caso o Department não exista, retorna nulo
				Department dep = map.get(rs.getInt("DepartmentId"));

				// Cria um novo objeto Deparment e o adiciona ao HashMap

				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}

				Seller obj = instantiateSeller(rs, dep);
				list.add(obj);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	
	// Faz uma busca a partir do Departamento
	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "WHERE DepartmentId = ? " + "ORDER BY Name");

			st.setInt(1, department.getId());// Recebe o Id do departamento

			rs = st.executeQuery();

			List<Seller> list = new ArrayList<>();

			// Map para armazenar todos os departamentos instanciados
			Map<Integer, Department> map = new HashMap<>();

			// Enquanto houver elementos

			while (rs.next()) {

				// Verifica se o objeto Department já existe pelo Id
				// Caso o Department não exista, retorna nulo
				Department dep = map.get(rs.getInt("DepartmentId"));

				// Cria um novo objeto Deparment e o adiciona ao HashMap

				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}

				Seller obj = instantiateSeller(rs, dep);
				list.add(obj);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}

	}

}
