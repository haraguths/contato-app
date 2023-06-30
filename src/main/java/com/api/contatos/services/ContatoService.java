package com.api.contatos.services;

import com.api.contatos.dtos.ContatoDto;
import com.api.contatos.dtos.EnderecoDto;
import com.api.contatos.models.Contato;
import com.api.contatos.models.Endereco;
import com.api.contatos.repositories.ContatoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
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
        Contato contato = new Contato();
        BeanUtils.copyProperties(contatoDto, contato);
        contato.setEnderecos(copiaPropriedadesEnderecos(contatoDto));
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

    public Contato updateParcialContato(UUID id, ContatoDto contatoDto) {
        Contato contato = getContato(id)
                .orElseThrow(() -> new NoSuchElementException("Contato não encontrado"));
        atualizarDadosContato(contato, contatoDto);
        atualizarEnderecosContato(contato, contatoDto.getEnderecos());
        return contatoRepository.save(contato);
    }

    private void atualizarEnderecosContato(Contato contato, List<EnderecoDto> enderecoDtos) {
        List<Endereco> enderecos = Optional.ofNullable(enderecoDtos)
                .orElse(Collections.emptyList())
                .stream()
                .map(enderecoDto -> atualizarEndereco(enderecoDto, contato))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        contato.setEnderecos(enderecos);
    }


    private Endereco atualizarEndereco(EnderecoDto enderecoDto, Contato contato) {
        if (Objects.nonNull(enderecoDto.getId())) {
            Optional<Endereco> enderecoExistente = contato.getEnderecos().stream()
                    .filter(endereco -> endereco.getId().equals(enderecoDto.getId()))
                    .findFirst();

            enderecoExistente.ifPresent(endereco -> {
                endereco.setNumero(enderecoDto.getNumero());
                endereco.setRua(enderecoDto.getRua());
                endereco.setCEP(enderecoDto.getCEP());
            });

            return enderecoExistente.orElse(null);
        } else {
            return convertToEntity(enderecoDto);
        }
    }

    private Endereco convertToEntity(EnderecoDto enderecoDto) {
        Endereco endereco = new Endereco();
        endereco.setNumero(enderecoDto.getNumero());
        endereco.setRua(enderecoDto.getRua());
        endereco.setCEP(enderecoDto.getCEP());
        return endereco;
    }

    private void atualizarDadosContato(Contato contato, ContatoDto contatoDto) {
        Optional.ofNullable(contatoDto.getNome()).ifPresent(contato::setNome);
        Optional.ofNullable(contatoDto.getEmail()).ifPresent(contato::setEmail);
        Optional.ofNullable(contatoDto.getTelefone()).ifPresent(contato::setTelefone);
        Optional.ofNullable(contatoDto.getDataNascimento()).ifPresent(contato::setDataNascimento);
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
