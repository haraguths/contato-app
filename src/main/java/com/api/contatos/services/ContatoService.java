package com.api.contatos.services;

import com.api.contatos.dtos.ContatoDto;
import com.api.contatos.models.Contato;
import com.api.contatos.models.Endereco;
import com.api.contatos.repositories.ContatoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContatoService {

    final
    ContatoRepository contatoRepository;

    public ContatoService(ContatoRepository contatoRepository) {
        this.contatoRepository = contatoRepository;
    }

    @Transactional
    public Contato salvar(ContatoDto contatoDto) {
        Contato contato = copiaPropriedadesContato(contatoDto);
        return contatoRepository.save(contato);
    }

    public Optional<Contato> getContato(UUID id) {
        return contatoRepository.findById(id);
    }

    public void deleteContato(UUID id) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Contato não encontrado"));

        contatoRepository.delete(contato);
    }

    private Contato copiaPropriedadesContato(ContatoDto contatoDto) {
        Contato contato = new Contato();
        BeanUtils.copyProperties(contatoDto, contato);
        contato.setEnderecos(copiaPropriedadesEnderecos(contatoDto));
        return contato;
    }

    private List<Endereco> copiaPropriedadesEnderecos(ContatoDto contatoDto) {
        List<Endereco> enderecoList = contatoDto.getEnderecos().stream()
                .map(enderecoDto -> {
                    Endereco endereco = new Endereco();
                    endereco.setNumero(enderecoDto.getNumero());
                    endereco.setRua(enderecoDto.getRua());
                    endereco.setCEP(enderecoDto.getCEP());
                    return endereco;
                })
                .collect(Collectors.toList());
        return enderecoList;
    }
}
