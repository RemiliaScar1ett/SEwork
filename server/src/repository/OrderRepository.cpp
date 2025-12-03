#include "repository/OrderRepository.h"
#include <fstream>
#include <sstream>
#include <iostream>
#include <cctype>

namespace
{
std::string trim(const std::string&s)
{
    size_t start=0;
    while(start<s.size()&&std::isspace((unsigned char)s[start]))start++;
    size_t end=s.size();
    while(end>start&&std::isspace((unsigned char)s[end-1]))end--;
    return s.substr(start,end-start);
}

std::vector<std::string>split(const std::string&s,char delim)
{
    std::vector<std::string>res;
    std::string cur;
    std::istringstream iss(s);
    while(std::getline(iss,cur,delim))
        res.push_back(cur);
    return res;
}

OrderStatus parseStatus(const std::string&s)
{
    std::string t=trim(s);
    for(char&c:t)c=(char)std::toupper((unsigned char)c);
    if(t=="PARTIAL")return OrderStatus::Partial;
    if(t=="CLOSED")return OrderStatus::Closed;
    return OrderStatus::New;
}

std::string statusToString(OrderStatus st)
{
    switch(st)
    {
    case OrderStatus::Partial: return "PARTIAL";
    case OrderStatus::Closed: return "CLOSED";
    case OrderStatus::New:
    default: return "NEW";
    }
}
}

void OrderRepository::loadFromCsv(const std::string&orderPath,const std::string&itemPath)
{
    orders.clear();
    items.clear();

    // 读 orders
    {
        std::ifstream ifs(orderPath);
        if(ifs.is_open())
        {
            std::string line;
            if(std::getline(ifs,line))
            {
                while(std::getline(ifs,line))
                {
                    if(line.empty())continue;
                    auto cols=split(line,',');
                    if(cols.size()<5)continue;
                    try
                    {
                        PurchaseOrder o;
                        o.id=std::stoi(trim(cols[0]));
                        o.supplierId=std::stoi(trim(cols[1]));
                        o.date=trim(cols[2]);
                        o.status=parseStatus(cols[3]);
                        o.createdBy=std::stoi(trim(cols[4]));
                        orders.push_back(o);
                    }
                    catch(...)
                    {
                        std::cerr<<"OrderRepository::loadFromCsv orders parse error line: "<<line<<"\n";
                    }
                }
            }
        }
    }

    // 读 order_items
    {
        std::ifstream ifs(itemPath);
        if(ifs.is_open())
        {
            std::string line;
            if(std::getline(ifs,line))
            {
                while(std::getline(ifs,line))
                {
                    if(line.empty())continue;
                    auto cols=split(line,',');
                    if(cols.size()<6)continue;
                    try
                    {
                        PurchaseOrderItem it;
                        it.id=std::stoi(trim(cols[0]));
                        it.orderId=std::stoi(trim(cols[1]));
                        it.materialId=std::stoi(trim(cols[2]));
                        it.quantity=std::stoi(trim(cols[3]));
                        it.price=std::stod(trim(cols[4]));
                        it.receivedQuantity=std::stoi(trim(cols[5]));
                        items.push_back(it);
                    }
                    catch(...)
                    {
                        std::cerr<<"OrderRepository::loadFromCsv items parse error line: "<<line<<"\n";
                    }
                }
            }
        }
    }
}

void OrderRepository::saveToCsv(const std::string&orderPath,const std::string&itemPath)const
{
    // 写 orders
    {
        std::ofstream ofs(orderPath,std::ios::trunc);
        if(!ofs.is_open())
        {
            std::cerr<<"OrderRepository::saveToCsv open failed: "<<orderPath<<"\n";
        }
        else
        {
            ofs<<"id,supplierId,date,status,createdBy\n";
            for(const auto&o:orders)
            {
                ofs<<o.id<<","<<o.supplierId<<","<<o.date<<","
                   <<statusToString(o.status)<<","<<o.createdBy<<"\n";
            }
        }
    }

    // 写 order_items
    {
        std::ofstream ofs(itemPath,std::ios::trunc);
        if(!ofs.is_open())
        {
            std::cerr<<"OrderRepository::saveToCsv open failed: "<<itemPath<<"\n";
        }
        else
        {
            ofs<<"id,orderId,materialId,quantity,price,receivedQuantity\n";
            for(const auto&it:items)
            {
                ofs<<it.id<<","<<it.orderId<<","<<it.materialId<<","
                   <<it.quantity<<","<<it.price<<","<<it.receivedQuantity<<"\n";
            }
        }
    }
}

std::optional<PurchaseOrder>OrderRepository::findOrderById(int id)const
{
    for(const auto&o:orders)
        if(o.id==id)return o;
    return std::nullopt;
}

std::vector<PurchaseOrder>OrderRepository::getAllOrders()const
{
    return orders;
}

int OrderRepository::addOrder(const PurchaseOrder&o)
{
    int id=nextOrderId();
    PurchaseOrder copy=o;
    copy.id=id;
    orders.push_back(copy);
    return id;
}

bool OrderRepository::updateOrder(const PurchaseOrder&o)
{
    for(auto&x:orders)
    {
        if(x.id==o.id)
        {
            x=o;
            return true;
        }
    }
    return false;
}

std::vector<PurchaseOrderItem>OrderRepository::findItemsByOrderId(int orderId)const
{
    std::vector<PurchaseOrderItem>res;
    for(const auto&it:items)
        if(it.orderId==orderId)res.push_back(it);
    return res;
}

std::optional<PurchaseOrderItem>OrderRepository::findItemById(int itemId)const
{
    for(const auto&it:items)
        if(it.id==itemId)return it;
    return std::nullopt;
}

int OrderRepository::addOrderItem(const PurchaseOrderItem&item)
{
    int id=nextOrderItemId();
    PurchaseOrderItem copy=item;
    copy.id=id;
    items.push_back(copy);
    return id;
}

bool OrderRepository::updateOrderItem(const PurchaseOrderItem&item)
{
    for(auto&x:items)
    {
        if(x.id==item.id)
        {
            x=item;
            return true;
        }
    }
    return false;
}

int OrderRepository::nextOrderId()const
{
    int maxId=0;
    for(const auto&o:orders)
        if(o.id>maxId)maxId=o.id;
    return maxId+1;
}

int OrderRepository::nextOrderItemId()const
{
    int maxId=0;
    for(const auto&it:items)
        if(it.id>maxId)maxId=it.id;
    return maxId+1;
}
