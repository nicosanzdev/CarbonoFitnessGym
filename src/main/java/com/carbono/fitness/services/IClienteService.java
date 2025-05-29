package com.carbono.fitness.services;

import com.carbono.fitness.entity.Cliente;

import java.util.List;

public interface IClienteService {

    public List<Cliente> listarClientes();

    public Cliente buscarClientePorId(Integer id);

    public void guardarCliente(Cliente cliente);

    public void eliminarCliente(Cliente cliente);

}
