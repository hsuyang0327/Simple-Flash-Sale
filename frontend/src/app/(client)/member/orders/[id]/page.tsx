import OrderDetail from '@/components/client/member/OrderDetail';

export default function OrderDetailPage({ params }: { params: { id: string } }) {
  return <OrderDetail orderId={params.id} />;
}
