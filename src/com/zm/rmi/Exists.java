package com.zm.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by zhangmin on 2016/4/12.
 */
public interface Exists extends Remote {
    public boolean fileExist(String filePath) throws RemoteException;
    public boolean DirectoryExist(String filePath) throws RemoteException;
    public boolean DirectoryHasFiles(String filePath) throws RemoteException;
}
